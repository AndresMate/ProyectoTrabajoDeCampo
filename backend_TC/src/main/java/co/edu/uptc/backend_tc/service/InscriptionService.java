package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.PlayerInscriptionDTO;
import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.dto.response.CategorySummaryDTO;
import co.edu.uptc.backend_tc.dto.response.ClubSummaryDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentSummaryDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.EntityNotFoundException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.InscriptionMapper;
import co.edu.uptc.backend_tc.mapper.PlayerMapper;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.repository.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final InscriptionPlayerRepository inscriptionPlayerRepository;
    private final InscriptionMapper inscriptionMapper;
    private final PlayerMapper playerMapper;
    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final TeamAvailabilityRepository teamAvailabilityRepository;


    public List<InscriptionResponseDTO> getAll() {
        return inscriptionRepository.findAll()
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    public List<InscriptionResponseDTO> getByTournament(Long tournamentId) {
        return inscriptionRepository.findByTournamentId(tournamentId)
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    public InscriptionResponseDTO getById(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));
        return enrichResponseDTO(inscription);
    }

    public List<InscriptionResponseDTO> getByDelegateEmail(String delegateEmail) {
        return inscriptionRepository.findByDelegateEmail(delegateEmail)
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    public List<InscriptionResponseDTO> getApprovedByTournament(Long tournamentId) {
        return inscriptionRepository.findByTournamentIdAndStatus(tournamentId, InscriptionStatus.APPROVED)
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    public boolean isTeamNameAvailable(Long tournamentId, String teamName) {
        return !inscriptionRepository.existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(
                tournamentId, teamName, InscriptionStatus.REJECTED);
    }

    @Transactional
    public InscriptionResponseDTO create(InscriptionDTO dto) {
        // ✅ 1. Validar torneo
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        if (tournament.getStatus() != TournamentStatus.OPEN_FOR_INSCRIPTION) {
            throw new BusinessException(
                    "Las inscripciones solo están permitidas para torneos con inscripción abierta",
                    "INSCRIPTIONS_CLOSED"
            );
        }

        // ✅ 2. Validar límite de equipos aprobados
        long currentTeams = inscriptionRepository.countByTournamentIdAndStatus(
                tournament.getId(), InscriptionStatus.APPROVED
        );
        if (tournament.getMaxTeams() != null && currentTeams >= tournament.getMaxTeams()) {
            throw new BusinessException("El torneo ha alcanzado el número máximo de equipos permitidos", "MAX_TEAMS_REACHED");
        }

        // ✅ 3. Validar categoría
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        if (!category.getSport().getId().equals(tournament.getSport().getId())) {
            throw new BusinessException("La categoría no pertenece al mismo deporte del torneo", "CATEGORY_SPORT_MISMATCH");
        }

        // ✅ 4. Validar nombre de equipo único en el torneo
        if (teamRepository.existsByTournamentAndNameIgnoreCase(tournament, dto.getTeamName())) {
            throw new ConflictException(
                    "El nombre del equipo ya está registrado en este torneo",
                    "teamName",
                    dto.getTeamName()
            );
        }

        // ✅ 5. Validar club único (si se selecciona)
        if (dto.getClubId() != null && teamRepository.existsByClubIdAndTournamentId(dto.getClubId(), tournament.getId())) {
            throw new ConflictException(
                    "Este club ya tiene un equipo inscrito en este torneo",
                    "clubId",
                    dto.getClubId()
            );
        }

        // ✅ 6. Validar jugadores duplicados en otros equipos del mismo torneo
        for (PlayerInscriptionDTO playerDTO : dto.getPlayers()) {
            if (playerRepository.existsByInstitutionalEmail(playerDTO.getInstitutionalEmail())) {
                Player existing = playerRepository.findByInstitutionalEmail(playerDTO.getInstitutionalEmail()).orElse(null);
                if (existing != null && teamRosterRepository.existsByPlayerIdAndTeam_TournamentId(existing.getId(), tournament.getId())) {
                    throw new ConflictException(
                            "El jugador " + playerDTO.getFullName() + " ya está inscrito en otro equipo de este torneo",
                            "players",
                            playerDTO.getInstitutionalEmail()
                    );
                }
            }
        }

        // ✅ 7. Validar índice del delegado
        if (dto.getDelegateIndex() == null || dto.getDelegateIndex() >= dto.getPlayers().size()) {
            throw new BusinessException("El índice del delegado no es válido", "INVALID_DELEGATE_INDEX");
        }

        // ✅ 8. Validar cantidad de jugadores
        Integer maxPlayers = category.getMembersPerTeam();
        if (maxPlayers != null && dto.getPlayers().size() > maxPlayers) {
            throw new BusinessException(
                    String.format("La categoría permite máximo %d jugadores, pero se enviaron %d",
                            maxPlayers, dto.getPlayers().size()),
                    "EXCEEDED_MAX_PLAYERS"
            );
        }

        if (dto.getPlayers().isEmpty()) {
            throw new BusinessException("Debe haber al menos un jugador en la inscripción", "NO_PLAYERS_PROVIDED");
        }

        // ✅ 9. Procesar jugadores
        List<Player> players = new ArrayList<>();
        Player delegate = null;

        for (int i = 0; i < dto.getPlayers().size(); i++) {
            PlayerInscriptionDTO playerDTO = dto.getPlayers().get(i);

            // Buscar jugador existente o crear uno nuevo
            Player player = playerRepository.findByDocumentNumber(playerDTO.getDocumentNumber())
                    .orElseGet(() -> {
                        // Validar email único
                        if (playerRepository.existsByInstitutionalEmail(playerDTO.getInstitutionalEmail())) {
                            throw new ConflictException(
                                    "Ya existe un jugador con este correo institucional",
                                    "institutionalEmail",
                                    playerDTO.getInstitutionalEmail()
                            );
                        }

                        // Validar código único
                        if (playerRepository.existsByStudentCode(playerDTO.getStudentCode())) {
                            throw new ConflictException(
                                    "Ya existe un jugador con este código estudiantil",
                                    "studentCode",
                                    playerDTO.getStudentCode()
                            );
                        }

                        Player newPlayer = Player.builder()
                                .documentNumber(playerDTO.getDocumentNumber())
                                .studentCode(playerDTO.getStudentCode())
                                .fullName(playerDTO.getFullName())
                                .institutionalEmail(playerDTO.getInstitutionalEmail())
                                .idCardPhotoUrl(playerDTO.getIdCardPhotoUrl())
                                .isActive(true)
                                .build();

                        return playerRepository.save(newPlayer);
                    });

            players.add(player);
            if (i == dto.getDelegateIndex()) {
                delegate = player;
            }
        }

        // ✅ 10. Obtener club si aplica
        Club club = null;
        if (dto.getClubId() != null) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));
        }

        // ✅ 11. Crear inscripción
        Inscription inscription = Inscription.builder()
                .tournament(tournament)
                .category(category)
                .teamName(dto.getTeamName())
                .delegate(delegate)
                .delegateName(delegate.getFullName())
                .delegateEmail(delegate.getInstitutionalEmail())
                .delegatePhone(dto.getDelegatePhone() != null ? dto.getDelegatePhone() : "")
                .club(club)
                .status(InscriptionStatus.PENDING)
                .build();

        inscription = inscriptionRepository.save(inscription);

        // ✅ 12. Asociar jugadores a la inscripción
        for (Player player : players) {
            InscriptionPlayer inscriptionPlayer = InscriptionPlayer.builder()
                    .inscription(inscription)
                    .player(player)
                    .build();
            inscriptionPlayerRepository.save(inscriptionPlayer);
        }

        return enrichResponseDTO(inscription);
    }


    @Transactional
    public InscriptionResponseDTO approve(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Inscription", inscriptionId));

        if (inscription.getStatus() == InscriptionStatus.APPROVED) {
            throw new BusinessException("La inscripción ya fue aprobada");
        }

        // Verifica que no exista equipo con el mismo nombre
        if (teamRepository.existsByTournamentAndName(inscription.getTournament(), inscription.getTeamName())) {
            throw new ConflictException("Ya existe un equipo con ese nombre en este torneo");
        }

        // Crear team
        Team team = Team.builder()
                .name(inscription.getTeamName())
                .tournament(inscription.getTournament())
                .category(inscription.getCategory())
                .club(inscription.getClub())
                .originInscription(inscription)
                .isActive(true)
                .build();
        teamRepository.save(team);

        // Migrar jugadores -> roster
        if (inscription.getPlayers() != null) {
            for (InscriptionPlayer ip : inscription.getPlayers()) {
                TeamRoster roster = TeamRoster.builder()
                        .team(team)
                        .player(ip.getPlayer())
                        .build();
                teamRosterRepository.save(roster);
            }
        }

        // Migrar disponibilidades de la inscripción al equipo
        List<TeamAvailability> inscriptionAvail = teamAvailabilityRepository.findByInscription(inscription);
        if (inscriptionAvail != null && !inscriptionAvail.isEmpty()) {
            for (TeamAvailability ta : inscriptionAvail) {
                ta.setTeam(team);
                ta.setInscription(null);
            }
            teamAvailabilityRepository.saveAll(inscriptionAvail);
        }

        // Marcar inscripción aprobada
        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription.setOriginatedTeam(team);
        inscriptionRepository.save(inscription);

        return inscriptionMapper.toResponseDTO(inscription);
    }

    // Exposed checks used by controller

    public boolean isClubRegistered(Long tournamentId, Long clubId) {
        if (clubId == null) return false;
        return inscriptionRepository.existsByTournamentIdAndClubIdAndStatusNot(tournamentId, clubId);
    }

    public Map<String, Object> checkPlayersAvailable(Long tournamentId, List<String> documents) {
        for (String doc : documents) {
            if (inscriptionRepository.existsByTournamentIdAndPlayerDocumentNumber(tournamentId, doc)) {
                return Map.of("isAllAvailable", false, "conflictingDocument", doc);
            }
        }
        return Map.of("isAllAvailable", true);
    }
    @Transactional
    public InscriptionResponseDTO reject(Long id, String reason) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING inscriptions can be rejected", "INVALID_STATUS_FOR_REJECTION");
        }

        inscription.setStatus(InscriptionStatus.REJECTED);
        inscription.setRejectionReason(reason);
        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    @Transactional
    public void delete(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (inscription.getStatus() == InscriptionStatus.APPROVED) {
            throw new BusinessException("Approved inscriptions cannot be deleted", "APPROVED_INSCRIPTION_DELETE_FORBIDDEN");
        }

        inscriptionRepository.delete(inscription);
    }

    /**
     * Verifica si un jugador ya está inscrito en otro equipo dentro del mismo torneo.
     *
     * @param tournamentId ID del torneo.
     * @param documentNumber Número de documento del jugador.
     * @return true si el jugador está disponible (no inscrito aún), false si ya está inscrito.
     */
    public boolean isPlayerAvailable(Long tournamentId, String documentNumber) {
        // Buscar inscripciones activas del torneo
        List<Inscription> inscriptions = inscriptionRepository.findByTournamentId(tournamentId);

        for (Inscription inscription : inscriptions) {
            // Si la inscripción tiene jugadores asociados, verificamos sus documentos
            List<InscriptionPlayer> players = inscriptionPlayerRepository.findByInscription(inscription);
            for (InscriptionPlayer ip : players) {
                if (ip.getPlayer() != null &&
                        ip.getPlayer().getDocumentNumber().equalsIgnoreCase(documentNumber)) {
                    return false; // ya está inscrito en otro equipo
                }
            }
        }

        return true; // no se encontró duplicado
    }


    private InscriptionResponseDTO enrichResponseDTO(Inscription inscription) {
        InscriptionResponseDTO response = InscriptionResponseDTO.builder()
                .id(inscription.getId())
                .teamName(inscription.getTeamName())
                .delegateName(inscription.getDelegateName())
                .delegateEmail(inscription.getDelegateEmail())
                .delegatePhone(inscription.getDelegatePhone())
                .status(inscription.getStatus())
                .rejectionReason(inscription.getRejectionReason())
                .createdAt(inscription.getCreatedAt())
                .updatedAt(inscription.getUpdatedAt())
                .build();

        response.setTournament(
                TournamentSummaryDTO.builder()
                        .id(inscription.getTournament().getId())
                        .name(inscription.getTournament().getName())
                        .status(inscription.getTournament().getStatus())
                        .build()
        );

        response.setCategory(
                CategorySummaryDTO.builder()
                        .id(inscription.getCategory().getId())
                        .name(inscription.getCategory().getName())
                        .membersPerTeam(inscription.getCategory().getMembersPerTeam())
                        .build()
        );

        if (inscription.getClub() != null) {
            response.setClub(
                    ClubSummaryDTO.builder()
                            .id(inscription.getClub().getId())
                            .name(inscription.getClub().getName())
                            .build()
            );
        }

        if (inscription.getPlayers() != null) {
            response.setPlayers(
                    inscription.getPlayers().stream()
                            .map(ip -> playerMapper.toSummaryDTO(ip.getPlayer()))
                            .collect(Collectors.toList())
            );
            response.setPlayerCount(inscription.getPlayers().size());
        } else {
            response.setPlayers(List.of());
            response.setPlayerCount(0);
        }

        return response;
    }
}