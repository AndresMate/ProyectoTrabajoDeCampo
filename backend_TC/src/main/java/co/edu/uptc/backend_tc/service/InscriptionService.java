package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.PlayerInscriptionDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
        // Validar torneo
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        if (tournament.getStatus() != TournamentStatus.OPEN_FOR_INSCRIPTION) {
            throw new BusinessException(
                    "Inscriptions are only allowed for tournaments with open registration",
                    "INSCRIPTIONS_CLOSED"
            );
        }

        // Validar límite de equipos
        long currentTeams = inscriptionRepository.countByTournamentIdAndStatus(
                tournament.getId(),
                InscriptionStatus.APPROVED
        );

        if (tournament.getMaxTeams() != null && currentTeams >= tournament.getMaxTeams()) {
            throw new BusinessException("Tournament has reached maximum number of teams", "MAX_TEAMS_REACHED");
        }

        // Validar categoría
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        if (!category.getSport().getId().equals(tournament.getSport().getId())) {
            throw new BusinessException("Category does not belong to tournament's sport", "CATEGORY_SPORT_MISMATCH");
        }

        // Validar nombre de equipo
        if (inscriptionRepository.existsByTournamentIdAndCategoryIdAndTeamNameIgnoreCase(
                tournament.getId(),
                category.getId(),
                dto.getTeamName())) {
            throw new ConflictException(
                    "A team with this name already exists in this tournament and category",
                    "teamName",
                    dto.getTeamName()
            );
        }

        // ✅ VALIDAR ÍNDICE DEL DELEGADO
        if (dto.getDelegateIndex() == null || dto.getDelegateIndex() >= dto.getPlayers().size()) {
            throw new BusinessException("Invalid delegate index", "INVALID_DELEGATE_INDEX");
        }

        // ✅ VALIDAR LÍMITE DE JUGADORES
        Integer maxPlayers = category.getMembersPerTeam();
        if (maxPlayers != null && dto.getPlayers().size() > maxPlayers) {
            throw new BusinessException(
                    String.format("Category allows maximum %d players, but %d were provided",
                            maxPlayers, dto.getPlayers().size()),
                    "EXCEEDED_MAX_PLAYERS"
            );
        }

        if (maxPlayers != null && dto.getPlayers().size() < 1) {
            throw new BusinessException(
                    "At least one player is required",
                    "INSUFFICIENT_PLAYERS"
            );
        }

        // ✅ PROCESAR JUGADORES
        List<Player> players = new ArrayList<>();
        PlayerInscriptionDTO delegateData = dto.getPlayers().get(dto.getDelegateIndex());
        Player delegate = null;

        for (int i = 0; i < dto.getPlayers().size(); i++) {
            PlayerInscriptionDTO playerDTO = dto.getPlayers().get(i);

            // Buscar o crear jugador
            Player player = playerRepository.findByDocumentNumber(playerDTO.getDocumentNumber())
                    .orElseGet(() -> {
                        // Validar email único
                        if (playerRepository.existsByInstitutionalEmail(playerDTO.getInstitutionalEmail())) {
                            throw new ConflictException(
                                    "A player with this email already exists",
                                    "institutionalEmail",
                                    playerDTO.getInstitutionalEmail()
                            );
                        }

                        // Validar código único
                        if (playerRepository.existsByStudentCode(playerDTO.getStudentCode())) {
                            throw new ConflictException(
                                    "A player with this student code already exists",
                                    "studentCode",
                                    playerDTO.getStudentCode()
                            );
                        }

                        // Crear nuevo jugador
                        Player newPlayer = Player.builder()
                                .documentNumber(playerDTO.getDocumentNumber())
                                .studentCode(playerDTO.getStudentCode())
                                .fullName(playerDTO.getFullName())
                                .institutionalEmail(playerDTO.getInstitutionalEmail())
                                .idCardPhotoUrl(playerDTO.getIdCardPhotoUrl()) // ✅ URL de la foto
                                .isActive(true)
                                .build();
                        return playerRepository.save(newPlayer);
                    });

            players.add(player);

            if (i == dto.getDelegateIndex()) {
                delegate = player;
            }
        }

        // Obtener club
        Club club = null;
        if (dto.getClubId() != null) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));
        }

        // ✅ CREAR INSCRIPCIÓN
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

        // ✅ ASOCIAR JUGADORES
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
        // 1️⃣ Buscar inscripción
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Inscripción", inscriptionId));

        if (inscription.getStatus() == InscriptionStatus.APPROVED) {
            throw new BusinessException("La inscripción ya fue aprobada");
        }

        // 2️⃣ Validar duplicado de nombre de equipo
        if (teamRepository.existsByTournamentAndName(inscription.getTournament(), inscription.getTeamName())) {
            throw new ConflictException("Ya existe un equipo con ese nombre en este torneo");
        }

        // 3️⃣ Crear el equipo oficial
        Team team = Team.builder()
                .name(inscription.getTeamName())
                .tournament(inscription.getTournament())
                .category(inscription.getCategory())
                .club(inscription.getClub())
                .originInscription(inscription)
                .isActive(true)
                .build();

        teamRepository.save(team);

        // 4️⃣ Migrar jugadores de inscripción a roster
        for (InscriptionPlayer inscriptionPlayer : inscription.getPlayers()) {
            TeamRoster roster = TeamRoster.builder()
                    .team(team)
                    .player(inscriptionPlayer.getPlayer())
                    .build();
            teamRosterRepository.save(roster);
        }

        // 5️⃣ Marcar inscripción como aprobada
        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription.setOriginatedTeam(team);
        inscriptionRepository.save(inscription);

        // 6️⃣ Retornar DTO
        return inscriptionMapper.toResponseDTO(inscription);
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