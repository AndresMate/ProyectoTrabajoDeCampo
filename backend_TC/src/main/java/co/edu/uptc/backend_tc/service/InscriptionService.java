package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.InscriptionStatsDTO;
import co.edu.uptc.backend_tc.dto.response.CategorySummaryDTO;
import co.edu.uptc.backend_tc.dto.response.ClubSummaryDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentSummaryDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ForbiddenException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.InscriptionMapper;
import co.edu.uptc.backend_tc.mapper.PlayerMapper;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final InscriptionMapper inscriptionMapper;
    private final PlayerMapper playerMapper;

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

    // Nuevo método para obtener inscripciones por email del delegado
    public List<InscriptionResponseDTO> getByDelegateEmail(String delegateEmail) {
        return inscriptionRepository.findByDelegateEmail(delegateEmail)
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    // Nuevo método para obtener inscripciones aprobadas por torneo
    public List<InscriptionResponseDTO> getApprovedByTournament(Long tournamentId) {
        return inscriptionRepository.findByTournamentIdAndStatus(tournamentId, InscriptionStatus.APPROVED)
                .stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    // Nuevo método para verificar disponibilidad de nombre de equipo
    public boolean isTeamNameAvailable(Long tournamentId, String teamName) {
        return !inscriptionRepository.existsByTournamentIdAndTeamNameIgnoreCaseAndStatusNot(
                tournamentId, teamName, InscriptionStatus.REJECTED);
    }

    @Transactional
    public InscriptionResponseDTO create(InscriptionDTO dto) {
        // Validar torneo
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        // ✅ CORRECCIÓN: Verificar estado correcto
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

        // ✅ CORRECCIÓN: Validar nombre de equipo duplicado
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

        // ✅ CORRECCIÓN: Validar email de delegado duplicado en la categoría
        List<Inscription> existingInscriptions = inscriptionRepository
                .findByTournamentIdAndCategoryId(tournament.getId(), category.getId());

        boolean delegateExists = existingInscriptions.stream()
                .anyMatch(i -> i.getDelegateEmail() != null &&
                        i.getDelegateEmail().equalsIgnoreCase(dto.getDelegateEmail()) &&
                        (i.getStatus() == InscriptionStatus.PENDING ||
                                i.getStatus() == InscriptionStatus.APPROVED));

        if (delegateExists) {
            throw new ConflictException(
                    "This email is already registered as a delegate in this tournament and category"
            );
        }

        // Obtener club si se proporcionó
        Club club = null;
        if (dto.getClubId() != null) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));
        }

        // ✅ Crear inscripción con los campos correctos
        Inscription inscription = Inscription.builder()
                .tournament(tournament)
                .category(category)
                .teamName(dto.getTeamName())
                .delegateName(dto.getDelegateName())
                .delegateEmail(dto.getDelegateEmail())
                .delegatePhone(dto.getDelegatePhone())
                .club(club)
                .status(InscriptionStatus.PENDING)
                .build();

        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }
    // Método actualizado sin parámetro User
    @Transactional
    public InscriptionResponseDTO approve(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING inscriptions can be approved", "INVALID_STATUS_FOR_APPROVAL");
        }

        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    // Método actualizado con parámetro reason
    @Transactional
    public InscriptionResponseDTO reject(Long id, String reason) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING inscriptions can be rejected", "INVALID_STATUS_FOR_REJECTION");
        }

        inscription.setStatus(InscriptionStatus.REJECTED);
        // Puedes agregar un campo para guardar el motivo del rechazo si lo necesitas
        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    // Método actualizado sin parámetro User
    @Transactional
    public void delete(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        // No permitir eliminar inscripciones ya aprobadas
        if (inscription.getStatus() == InscriptionStatus.APPROVED) {
            throw new BusinessException("Approved inscriptions cannot be deleted", "APPROVED_INSCRIPTION_DELETE_FORBIDDEN");
        }

        inscriptionRepository.delete(inscription);
    }


    // Método auxiliar para armar correctamente el DTO de respuesta
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

        // Tournament info
        response.setTournament(
                TournamentSummaryDTO.builder()
                        .id(inscription.getTournament().getId())
                        .name(inscription.getTournament().getName())
                        .status(inscription.getTournament().getStatus())
                        .build()
        );

        // Category info
        response.setCategory(
                CategorySummaryDTO.builder()
                        .id(inscription.getCategory().getId())
                        .name(inscription.getCategory().getName())
                        .membersPerTeam(inscription.getCategory().getMembersPerTeam())
                        .build()
        );

        // Club info (opcional)
        if (inscription.getClub() != null) {
            response.setClub(
                    ClubSummaryDTO.builder()
                            .id(inscription.getClub().getId())
                            .name(inscription.getClub().getName())
                            .build()
            );
        }

        // Jugadores inscritos
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