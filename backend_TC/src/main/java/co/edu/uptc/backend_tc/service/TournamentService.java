package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.TournamentStatsDTO;
import co.edu.uptc.backend_tc.entity.Category;
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
    private final CategoryRepository categoryRepository; // ✅ agregado
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
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        Sport sport = sportRepository.findById(dto.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        if (!category.getSport().getId().equals(dto.getSportId())) {
            throw new BusinessException("Category does not belong to the selected sport", "CATEGORY_SPORT_MISMATCH");
        }

        User creator = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));

        Tournament tournament = Tournament.builder()
                .name(dto.getName())
                .maxTeams(dto.getMaxTeams())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .modality(dto.getModality()) // <-- aquí
                .status(TournamentStatus.PLANNING)
                .sport(sport)
                .category(category)
                .createdBy(creator)
                .build();

        tournament = tournamentRepository.save(tournament);
        return tournamentMapper.toResponseDTO(tournament);
    }


    @Transactional
    public TournamentResponseDTO update(Long id, TournamentDTO dto) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // ⚙️ Solo actualizamos lo que venga en el DTO (no todo)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            tournament.setName(dto.getName());
        }

        if (dto.getStartDate() != null) {
            tournament.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            tournament.setEndDate(dto.getEndDate());
        }

        if (dto.getMaxTeams() != null) {
            tournament.setMaxTeams(dto.getMaxTeams());
        }

        if (dto.getModality() != null) {
            tournament.setModality(dto.getModality());
        }

        if (dto.getStatus() != null) {
            tournament.setStatus(dto.getStatus());
        }

        if (dto.getSportId() != null) {
            Sport sport = sportRepository.findById(dto.getSportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));
            tournament.setSport(sport);
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            tournament.setCategory(category);
        }

        if (dto.getCreatedById() != null) {
            User creator = userRepository.findById(dto.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getCreatedById()));
            tournament.setCreatedBy(creator);
        }

        // 💾 Guardar solo los cambios
        Tournament updated = tournamentRepository.save(tournament);
        return tournamentMapper.toResponseDTO(updated);
    }


    @Transactional
    public TournamentResponseDTO startTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        if (tournament.getStatus() != TournamentStatus.PLANNING) {
            throw new BusinessException(
                    "Tournament can only be started from PLANNING status",
                    "INVALID_STATUS_TRANSITION"
            );
        }

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

        long matchCount = matchRepository.countByTournamentId(id);
        if (matchCount > 0 && tournament.getStatus() != TournamentStatus.PLANNING) {
            throw new BusinessException(
                    "Cannot delete tournament with matches played",
                    "HAS_PLAYED_MATCHES"
            );
        }

        tournamentRepository.delete(tournament);
    }

    private TournamentResponseDTO enrichResponseDTO(Tournament tournament) {
        TournamentResponseDTO dto = tournamentMapper.toResponseDTO(tournament);
        dto.setCurrentTeamCount((int) teamRepository.countByTournamentId(tournament.getId()));
        dto.setTotalMatches((int) matchRepository.countByTournamentId(tournament.getId()));
        dto.setCompletedMatches((int) matchRepository.countByTournamentIdAndStatus(
                tournament.getId(),
                co.edu.uptc.backend_tc.model.MatchStatus.FINISHED
        ));
        return dto;
    }

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
                        root.get("startDate"), filter.getStartDateFrom()
                ));
            }

            if (filter.getStartDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("startDate"), filter.getStartDateTo()
                ));
            }

            if (filter.getCreatedById() != null) {
                predicates.add(cb.equal(
                        root.get("createdBy").get("id"), filter.getCreatedById()
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public List<TournamentResponseDTO> findActiveTournaments() {
        List<Tournament> tournaments = tournamentRepository.findByStatusIn(
                List.of(TournamentStatus.OPEN_FOR_INSCRIPTION, TournamentStatus.IN_PROGRESS)
        );
        return tournaments.stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TournamentResponseDTO cancelTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        if (tournament.getStatus() == TournamentStatus.FINISHED) {
            throw new BusinessException(
                    "Cannot cancel completed tournament",
                    "TOURNAMENT_COMPLETED"
            );
        }

        tournament.setStatus(TournamentStatus.CANCELLED);
        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    public List<TournamentResponseDTO> findInProgressTournaments() {
        List<Tournament> tournaments = tournamentRepository.findByStatus(TournamentStatus.IN_PROGRESS);
        return tournaments.stream()
                .map(this::enrichResponseDTO)
                .collect(Collectors.toList());
    }

    public TournamentStatsDTO getTournamentStats() {
        long totalTournaments = tournamentRepository.count();
        long planningCount = tournamentRepository.countByStatus(TournamentStatus.PLANNING);
        long inProgressCount = tournamentRepository.countByStatus(TournamentStatus.IN_PROGRESS);
        long finishedCount = tournamentRepository.countByStatus(TournamentStatus.FINISHED);
        long cancelledCount = tournamentRepository.countByStatus(TournamentStatus.CANCELLED);

        return TournamentStatsDTO.builder()
                .totalTournaments(totalTournaments)
                .planningCount(planningCount)
                .inProgressCount(inProgressCount)
                .finishedCount(finishedCount)
                .cancelledCount(cancelledCount)
                .build();
    }
}
