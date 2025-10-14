package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.TournamentMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TournamentMapper tournamentMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<TournamentResponseDTO> getAll(Pageable pageable) {
        Page<Tournament> page = tournamentRepository.findAll(pageable);
        return mapperUtils.mapPage(page, this::enrichResponseDTO);
    }

    public PageResponseDTO<TournamentResponseDTO> search(
            TournamentFilterDTO filter,
            Pageable pageable) {
        Specification<Tournament> spec = buildSpecification(filter);
        Page<Tournament> page = tournamentRepository.findAll(spec, pageable);
        return mapperUtils.mapPage(page, this::enrichResponseDTO);
    }

    public TournamentResponseDTO getById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));
        return enrichResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO create(TournamentDTO dto) {
        // Validaciones de negocio
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (dto.getStartDate().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Start date must be in the future");
        }

        // Obtener entidades relacionadas
        Sport sport = sportRepository.findById(dto.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));

        if (!sport.getIsActive()) {
            throw new BusinessException(
                    "Cannot create tournament for inactive sport",
                    "SPORT_INACTIVE"
            );
        }

        User creator = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));

        // Crear torneo
        Tournament tournament = Tournament.builder()
                .name(dto.getName())
                .maxTeams(dto.getMaxTeams())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .modality(dto.getModality())
                .status(TournamentStatus.PLANNING)
                .sport(sport)
                .createdBy(creator)
                .build();

        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO update(Long id, TournamentDTO dto) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // Validaciones de estado
        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            throw new BusinessException(
                    "Cannot update completed tournament",
                    "TOURNAMENT_COMPLETED"
            );
        }

        // Validar fechas
        if (dto.getStartDate() != null && dto.getEndDate() != null &&
                dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        // Obtener entidades relacionadas si cambiaron
        Sport sport = null;
        if (dto.getSportId() != null && !dto.getSportId().equals(tournament.getSport().getId())) {
            sport = sportRepository.findById(dto.getSportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));
        }

        User creator = null;
        if (dto.getCreatedById() != null &&
                !dto.getCreatedById().equals(tournament.getCreatedBy().getId())) {
            creator = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));
        }

        tournamentMapper.updateEntityFromDTO(dto, tournament, sport, creator);
        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO startTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // Validar estado actual
        if (tournament.getStatus() != TournamentStatus.PLANNING) {
            throw new BusinessException(
                    "Tournament can only be started from PLANNING status",
                    "INVALID_STATUS_TRANSITION"
            );
        }

        // Verificar que hay equipos suficientes
        long teamCount = teamRepository.countByTournamentId(id);
        if (teamCount < 2) {
            throw new BusinessException(
                    "Tournament needs at least 2 teams to start",
                    "INSUFFICIENT_TEAMS"
            );
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    @Transactional
    public TournamentResponseDTO completeTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "Only tournaments in progress can be completed",
                    "INVALID_STATUS_TRANSITION"
            );
        }

        tournament.setStatus(TournamentStatus.FINISHED);
        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    @Transactional
    public void delete(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // Verificar que se puede eliminar
        long matchCount = matchRepository.countByTournamentId(id);
        if (matchCount > 0 && tournament.getStatus() != TournamentStatus.PLANNING) {
            throw new BusinessException(
                    "Cannot delete tournament with matches played",
                    "HAS_PLAYED_MATCHES"
            );
        }

        tournamentRepository.delete(tournament);
    }

    // Método privado para enriquecer el DTO con estadísticas
    private TournamentResponseDTO enrichResponseDTO(Tournament tournament) {
        TournamentResponseDTO dto = tournamentMapper.toResponseDTO(tournament);

        // Agregar estadísticas calculadas
        dto.setCurrentTeamCount((int) teamRepository.countByTournamentId(tournament.getId()));
        dto.setTotalMatches((int) matchRepository.countByTournamentId(tournament.getId()));
        dto.setCompletedMatches((int) matchRepository.countByTournamentIdAndStatus(
                tournament.getId(),
                co.edu.uptc.backend_tc.model.MatchStatus.FINISHED
        ));


        return dto;
    }

    // Método helper para construir especificaciones de búsqueda
    private Specification<Tournament> buildSpecification(TournamentFilterDTO filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getModality() != null) {
                predicates.add(cb.equal(root.get("modality"), filter.getModality()));
            }

            if (filter.getSportId() != null) {
                predicates.add(cb.equal(root.get("sport").get("id"), filter.getSportId()));
            }

            if (filter.getStartDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("startDate"),
                        filter.getStartDateFrom()
                ));
            }

            if (filter.getStartDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("startDate"),
                        filter.getStartDateTo()
                ));
            }

            if (filter.getCreatedById() != null) {
                predicates.add(cb.equal(
                        root.get("createdBy").get("id"),
                        filter.getCreatedById()
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public List<TournamentResponseDTO> findActiveTournaments() {
        // Opción 1: Usando findByStatusIn
        List<Tournament> tournaments = tournamentRepository.findByStatusIn(
                List.of(TournamentStatus.OPEN_FOR_INSCRIPTION, TournamentStatus.IN_PROGRESS)
        );
        return tournaments.stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

}