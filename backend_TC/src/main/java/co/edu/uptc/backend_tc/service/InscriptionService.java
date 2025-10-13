package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.response.CategorySummaryDTO;
import co.edu.uptc.backend_tc.dto.response.ClubSummaryDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
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

    @Transactional
    public InscriptionResponseDTO create(InscriptionDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", dto.getTournamentId()));

        if (tournament.getStatus() != TournamentStatus.PLANNING) {
            throw new BusinessException("Inscriptions are only allowed for tournaments in PLANNING status", "INSCRIPTIONS_CLOSED");
        }

        long currentTeams = inscriptionRepository.countByTournamentIdAndStatus(
                tournament.getId(),
                InscriptionStatus.APPROVED
        );

        if (tournament.getMaxTeams() != null && currentTeams >= tournament.getMaxTeams()) {
            throw new BusinessException("Tournament has reached maximum number of teams", "MAX_TEAMS_REACHED");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        if (!category.getSport().getId().equals(tournament.getSport().getId())) {
            throw new BusinessException("Category does not belong to tournament's sport", "CATEGORY_SPORT_MISMATCH");
        }

        Player delegate = playerRepository.findById(dto.getDelegatePlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", dto.getDelegatePlayerId()));

        if (!delegate.getIsActive()) {
            throw new BusinessException("Delegate player must be active", "INACTIVE_DELEGATE");
        }

        if (inscriptionRepository.existsByTournamentIdAndCategoryIdAndDelegateId(
                tournament.getId(),
                category.getId(),
                delegate.getId())) {
            throw new ConflictException("This player already has an inscription as delegate in this tournament and category");
        }

        if (inscriptionRepository.existsByTournamentIdAndCategoryIdAndTeamNameIgnoreCase(
                tournament.getId(),
                category.getId(),
                dto.getTeamName())) {
            throw new ConflictException("A team with this name already exists in this tournament and category", "teamName", dto.getTeamName());
        }

        Club club = null;
        if (dto.getClubId() != null) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", dto.getClubId()));
        }

        Inscription inscription = Inscription.builder()
                .tournament(tournament)
                .category(category)
                .teamName(dto.getTeamName())
                .delegate(delegate)
                .delegatePhone(dto.getDelegatePhone())
                .club(club)
                .status(InscriptionStatus.PENDING)
                .build();

        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    @Transactional
    public InscriptionResponseDTO approve(Long id, User approver) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (approver.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("inscription", "approve");
        }

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING inscriptions can be approved", "INVALID_STATUS_FOR_APPROVAL");
        }

        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    @Transactional
    public InscriptionResponseDTO reject(Long id, User rejector) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        if (rejector.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("inscription", "reject");
        }

        if (inscription.getStatus() != InscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING inscriptions can be rejected", "INVALID_STATUS_FOR_REJECTION");
        }

        inscription.setStatus(InscriptionStatus.REJECTED);
        inscription = inscriptionRepository.save(inscription);
        return enrichResponseDTO(inscription);
    }

    @Transactional
    public void delete(Long id, User deleter) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", "id", id));

        // Solo el delegado o el ADMIN pueden eliminar
        if (!inscription.getDelegate().getId().equals(deleter.getId()) &&
                deleter.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("inscription", "delete");
        }

        // No permitir eliminar inscripciones ya aprobadas
        if (inscription.getStatus() == InscriptionStatus.APPROVED) {
            throw new BusinessException("Approved inscriptions cannot be deleted", "APPROVED_INSCRIPTION_DELETE_FORBIDDEN");
        }

        inscriptionRepository.delete(inscription);
    }

    // Método auxiliar para armar correctamente el DTO de respuesta
    private InscriptionResponseDTO enrichResponseDTO(Inscription inscription) {
        InscriptionResponseDTO response = inscriptionMapper.toResponseDTO(inscription);

        // Asignar información anidada
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
                        .build()
        );

        response.setDelegate(playerMapper.toSummaryDTO(inscription.getDelegate()));

        if (inscription.getClub() != null) {
            response.setClub(
                    ClubSummaryDTO.builder()
                            .id(inscription.getClub().getId())
                            .name(inscription.getClub().getName())
                            .build()
            );
        }

        // Lista de jugadores inscritos (si tu entidad Inscription tiene relación con Player)
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
