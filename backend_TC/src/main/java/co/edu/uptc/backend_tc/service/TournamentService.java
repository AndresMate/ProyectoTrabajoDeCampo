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

import java.time.LocalDate;
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
        // Validar que startDate no sea pasada
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "La fecha de inicio del torneo no puede ser anterior a la fecha actual. Por favor, seleccione una fecha de hoy en adelante",
                    "START_DATE_IN_PAST"
            );
        }
        
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException(
                    "La fecha de fin del torneo debe ser posterior a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio",
                    "END_DATE_BEFORE_START_DATE"
            );
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

        // Calcular fechas de inscripción sugeridas si no vienen en el DTO
        LocalDate inscriptionStartDate = dto.getInscriptionStartDate();
        LocalDate inscriptionEndDate = dto.getInscriptionEndDate();
        
        if (inscriptionStartDate == null && inscriptionEndDate == null) {
            // Calcular fechas sugeridas: 30 días antes del inicio
            inscriptionStartDate = dto.getStartDate().minusDays(30);
            inscriptionEndDate = dto.getStartDate().minusDays(1);
        } else if (inscriptionStartDate == null && inscriptionEndDate != null) {
            // Si solo viene inscriptionEndDate, calcular inscriptionStartDate
            inscriptionStartDate = inscriptionEndDate.minusDays(30);
        } else if (inscriptionStartDate != null && inscriptionEndDate == null) {
            // Si solo viene inscriptionStartDate, calcular inscriptionEndDate
            inscriptionEndDate = dto.getStartDate().minusDays(1);
        }

        // Validar y corregir fechas si es necesario
        if (inscriptionStartDate != null && inscriptionEndDate != null) {
            if (inscriptionEndDate.isBefore(inscriptionStartDate)) {
                // Corregir: inscriptionEndDate debe ser posterior a inscriptionStartDate
                inscriptionEndDate = inscriptionStartDate.plusDays(30);
            }
            if (!inscriptionEndDate.isBefore(dto.getStartDate())) {
                // Corregir: inscriptionEndDate debe ser anterior a startDate
                inscriptionEndDate = dto.getStartDate().minusDays(1);
            }
        }

        Tournament tournament = Tournament.builder()
                .name(dto.getName())
                .maxTeams(dto.getMaxTeams())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .inscriptionStartDate(inscriptionStartDate)
                .inscriptionEndDate(inscriptionEndDate)
                .modality(dto.getModality())
                .status(TournamentStatus.PLANNING)
                .sport(sport)
                .category(category)
                .createdBy(creator)
                .build();

        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
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
            LocalDate newStartDate = dto.getStartDate();
            LocalDate today = LocalDate.now();
            
            // Validar que la nueva fecha no sea pasada (excepto para torneos finalizados o cancelados)
            if (newStartDate.isBefore(today)) {
                // Solo permitir fechas pasadas si el torneo ya está finalizado o cancelado (para corrección de datos históricos)
                if (tournament.getStatus() != TournamentStatus.FINISHED 
                        && tournament.getStatus() != TournamentStatus.CANCELLED) {
                    throw new BusinessException(
                            "No se puede cambiar la fecha de inicio a una fecha pasada. La fecha de inicio debe ser hoy o una fecha futura",
                            "INVALID_START_DATE_PAST"
                    );
                }
            }
            
            // Validar que si el torneo está en OPEN_FOR_INSCRIPTION, la nueva fecha no sea pasada
            if (tournament.getStatus() == TournamentStatus.OPEN_FOR_INSCRIPTION 
                    && newStartDate.isBefore(today)) {
                throw new BusinessException(
                        "No se puede cambiar la fecha de inicio a una fecha pasada cuando el torneo está en inscripciones abiertas. Por favor, cambie el estado del torneo primero",
                        "INVALID_START_DATE_FOR_OPEN_INSCRIPTIONS"
                );
            }
            
            tournament.setStartDate(newStartDate);
            // Si se cambia startDate, validar y corregir fechas de inscripción si es necesario
            validateAndCorrectInscriptionDates(tournament);
        }

        if (dto.getEndDate() != null) {
            LocalDate newEndDate = dto.getEndDate();
            LocalDate currentStartDate = tournament.getStartDate();
            
            // Validar que endDate sea posterior o igual a startDate
            if (newEndDate.isBefore(currentStartDate)) {
                throw new BusinessException(
                        "La fecha de fin del torneo debe ser posterior o igual a la fecha de inicio. Por favor, seleccione una fecha de fin que sea igual o posterior a la fecha de inicio (" + 
                        currentStartDate.toString() + ")",
                        "END_DATE_BEFORE_START_DATE"
                );
            }
            
            tournament.setEndDate(newEndDate);
        }

        if (dto.getInscriptionStartDate() != null) {
            tournament.setInscriptionStartDate(dto.getInscriptionStartDate());
            validateAndCorrectInscriptionDates(tournament);
        }

        if (dto.getInscriptionEndDate() != null) {
            tournament.setInscriptionEndDate(dto.getInscriptionEndDate());
            validateAndCorrectInscriptionDates(tournament);
        }

        if (dto.getMaxTeams() != null) {
            tournament.setMaxTeams(dto.getMaxTeams());
        }

        if (dto.getModality() != null) {
            tournament.setModality(dto.getModality());
        }

        // Validar estado antes de cambiarlo
        if (dto.getStatus() != null) {
            validateAndCorrectDatesForStatusChange(tournament, dto.getStatus());
            
            // Si se intenta cambiar a OPEN_FOR_INSCRIPTION y startDate ya pasó, rechazar o cambiar a IN_PROGRESS
            if (dto.getStatus() == TournamentStatus.OPEN_FOR_INSCRIPTION 
                    && tournament.getStartDate().isBefore(LocalDate.now())) {
                throw new BusinessException(
                        "No se puede cambiar el estado a 'Inscripciones Abiertas' cuando la fecha de inicio del torneo ya pasó. El torneo debe estar en 'En Curso' o 'Finalizado'",
                        "CANNOT_OPEN_INSCRIPTIONS_PAST_START_DATE"
                );
            }
            
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
        return enrichResponseDTO(updated);
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

        // Validar y corregir fechas de inscripción antes de iniciar
        validateAndCorrectInscriptionDates(tournament);

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);
        return enrichResponseDTO(tournament);
    }

    /**
     * Abre las inscripciones del torneo (cambia estado a OPEN_FOR_INSCRIPTION)
     * Valida y corrige fechas de inscripción si es necesario
     */
    @Transactional
    public TournamentResponseDTO openInscriptions(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", id));

        // Validar que startDate no sea pasada
        if (tournament.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "No se pueden abrir inscripciones para un torneo cuya fecha de inicio ya pasó. El torneo debe iniciarse directamente",
                    "CANNOT_OPEN_INSCRIPTIONS_PAST_START_DATE"
            );
        }

        // Validar que las fechas de inscripción existan
        if (tournament.getInscriptionStartDate() == null || tournament.getInscriptionEndDate() == null) {
            // Calcular fechas automáticamente si no existen
            if (tournament.getInscriptionStartDate() == null) {
                tournament.setInscriptionStartDate(tournament.getStartDate().minusDays(30));
            }
            if (tournament.getInscriptionEndDate() == null) {
                tournament.setInscriptionEndDate(tournament.getStartDate().minusDays(1));
            }
        }

        // Validar y corregir coherencia de fechas
        validateAndCorrectInscriptionDates(tournament);

        // Validar orden de fechas
        if (tournament.getInscriptionEndDate().isAfter(tournament.getStartDate()) || 
            tournament.getInscriptionEndDate().isEqual(tournament.getStartDate())) {
            throw new BusinessException(
                    "La fecha de cierre de inscripciones debe ser anterior al inicio del torneo",
                    "INVALID_INSCRIPTION_DATES"
            );
        }

        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
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

        long totalMatches = matchRepository.countByTournamentId(id);
        long finishedMatches = matchRepository.countByTournamentIdAndStatus(
                id,
                co.edu.uptc.backend_tc.model.MatchStatus.FINISHED
        );

        if (totalMatches > 0 && finishedMatches < totalMatches) {
            throw new BusinessException(
                    "Cannot complete tournament: there are pending matches",
                    "PENDING_MATCHES"
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

    /**
     * Valida y corrige automáticamente las fechas de inscripción para mantener coherencia
     */
    private void validateAndCorrectInscriptionDates(Tournament tournament) {
        if (tournament.getInscriptionStartDate() == null || tournament.getInscriptionEndDate() == null) {
            return; // No hacer nada si alguna fecha es NULL
        }

        LocalDate inscriptionStart = tournament.getInscriptionStartDate();
        LocalDate inscriptionEnd = tournament.getInscriptionEndDate();
        LocalDate startDate = tournament.getStartDate();

        // Corrección 1: inscriptionEndDate debe ser posterior a inscriptionStartDate
        if (inscriptionEnd.isBefore(inscriptionStart) || inscriptionEnd.isEqual(inscriptionStart)) {
            // Ajustar inscriptionEndDate a 30 días después de inscriptionStartDate
            tournament.setInscriptionEndDate(inscriptionStart.plusDays(30));
            inscriptionEnd = tournament.getInscriptionEndDate();
        }

        // Corrección 2: inscriptionEndDate debe ser anterior a startDate
        if (!inscriptionEnd.isBefore(startDate)) {
            // Ajustar inscriptionEndDate a 1 día antes de startDate
            tournament.setInscriptionEndDate(startDate.minusDays(1));
            inscriptionEnd = tournament.getInscriptionEndDate();
            
            // Si después de la corrección, inscriptionStartDate es posterior a inscriptionEndDate, ajustar también
            if (!inscriptionStart.isBefore(inscriptionEnd)) {
                tournament.setInscriptionStartDate(inscriptionEnd.minusDays(30));
            }
        }
    }

    /**
     * Valida y corrige fechas cuando se cambia el estado del torneo
     */
    private void validateAndCorrectDatesForStatusChange(Tournament tournament, TournamentStatus newStatus) {
        if (newStatus == TournamentStatus.OPEN_FOR_INSCRIPTION) {
            // Validar que startDate no sea pasada
            if (tournament.getStartDate().isBefore(LocalDate.now())) {
                throw new BusinessException(
                        "No se puede cambiar el estado a 'Inscripciones Abiertas' cuando la fecha de inicio del torneo ya pasó",
                        "CANNOT_OPEN_INSCRIPTIONS_PAST_START_DATE"
                );
            }
            
            // Validar que las fechas de inscripción existan
            if (tournament.getInscriptionStartDate() == null || tournament.getInscriptionEndDate() == null) {
                throw new BusinessException(
                        "Para abrir inscripciones, debe definir las fechas de inscripción",
                        "MISSING_INSCRIPTION_DATES"
                );
            }
            
            // Validar y corregir coherencia
            validateAndCorrectInscriptionDates(tournament);
            
            // Validar que inscriptionEndDate sea anterior a startDate
            if (!tournament.getInscriptionEndDate().isBefore(tournament.getStartDate())) {
                throw new BusinessException(
                        "La fecha de cierre de inscripciones debe ser anterior al inicio del torneo",
                        "INVALID_INSCRIPTION_DATES"
                );
            }
        } else if (newStatus == TournamentStatus.IN_PROGRESS) {
            // Si hay fechas de inscripción, validar que sean coherentes
            if (tournament.getInscriptionEndDate() != null) {
                validateAndCorrectInscriptionDates(tournament);
            }
        }
    }
}
