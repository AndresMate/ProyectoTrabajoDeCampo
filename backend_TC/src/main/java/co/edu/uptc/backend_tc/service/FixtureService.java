package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixtureService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final CategoryRepository categoryRepository;
    private final MatchRepository matchRepository;
    private final TeamAvailabilityRepository availabilityRepository;

    /**
     * Genera el fixture de un torneo según el modo seleccionado.
     */
    @Transactional
    public int generateFixture(Long tournamentId, Long categoryId, String mode) {
        // Validar parámetros de entrada
        if (tournamentId == null) {
            throw new BadRequestException("Tournament ID is required");
        }
        if (categoryId == null) {
            throw new BadRequestException("Category ID is required");
        }
        if (mode == null || mode.trim().isEmpty()) {
            throw new BadRequestException("Mode is required");
        }

        // Verificar torneo
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", tournamentId));

        // Validar que el torneo no esté en estado inconsistente (OPEN_FOR_INSCRIPTION con startDate pasada)
        if (tournament.getStatus() == TournamentStatus.OPEN_FOR_INSCRIPTION 
                && tournament.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "No se puede generar el fixture: el torneo está en estado 'Inscripciones Abiertas' pero la fecha de inicio ya pasó. Por favor, cambie el estado del torneo a 'En Curso' primero",
                    "INCONSISTENT_TOURNAMENT_STATUS"
            );
        }

        // Verificar categoría
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Filtrar equipos aprobados
        List<Team> teams = teamRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId)
                .stream()
                .filter(t -> t.getOriginInscription() != null &&
                        t.getOriginInscription().getStatus() == InscriptionStatus.APPROVED)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new BusinessException("No hay suficientes equipos aprobados (mínimo 2)", "INSUFFICIENT_TEAMS");
        }

        // Validar que no haya partidos en curso o finalizados
        List<Match> existingMatches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        boolean hasInProgressOrFinished = existingMatches.stream()
                .anyMatch(m -> m.getStatus() == MatchStatus.IN_PROGRESS || m.getStatus() == MatchStatus.FINISHED);
        if (hasInProgressOrFinished) {
            throw new BusinessException("No se puede regenerar el fixture: existen partidos en curso o finalizados",
                    "FIXTURE_ALREADY_STARTED");
        }

        // Eliminar fixture existente
        deleteFixture(tournamentId, categoryId);

        // Normalizar modo
        mode = mode.trim().toLowerCase();

        List<Match> generatedMatches;
        switch (mode) {
            case "round_robin" -> generatedMatches = generateRoundRobin(tournament, category, teams);
            case "knockout" -> generatedMatches = generateKnockout(tournament, category, teams);
            default -> throw new BadRequestException("Modo de fixture inválido: " + mode);
        }

        // Validar que la lista no sea null antes de guardar
        if (generatedMatches == null || generatedMatches.isEmpty()) {
            return 0;
        }

        matchRepository.saveAll(generatedMatches);
        return generatedMatches.size();
    }

    /**
     * Genera un fixture tipo "todos contra todos".
     */
    private List<Match> generateRoundRobin(Tournament tournament, Category category, List<Team> teams) {
        int numTeams = teams.size();
        boolean hasBye = (numTeams % 2 != 0);

        if (hasBye) teams.add(null); // bye para impar

        int rounds = teams.size() - 1;
        int matchesPerRound = teams.size() / 2;
        List<Match> matches = new ArrayList<>();
        List<Team> rotated = new ArrayList<>(teams);

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                Team home = rotated.get(i);
                Team away = rotated.get(rotated.size() - 1 - i);

                if (home == null || away == null) continue; // bye

                LocalDateTime matchTime = findCompatibleSlot(home, away, tournament, matches);

                matches.add(Match.builder()
                        .tournament(tournament)
                        .category(category)
                        .homeTeam(home)
                        .awayTeam(away)
                        .status(MatchStatus.SCHEDULED)
                        .startsAt(matchTime)
                        .build());
            }

            // Rotar los equipos, dejando el primero fijo
            rotated.add(1, rotated.remove(rotated.size() - 1));
        }

        return matches;
    }

    /**
     * Genera un fixture tipo eliminación directa.
     */
    private List<Match> generateKnockout(Tournament tournament, Category category, List<Team> teams) {
        Collections.shuffle(teams);
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            if (i + 1 >= teams.size()) break; // impar queda libre

            Team home = teams.get(i);
            Team away = teams.get(i + 1);

            LocalDateTime matchTime = findCompatibleSlot(home, away, tournament, matches);

            matches.add(Match.builder()
                    .tournament(tournament)
                    .category(category)
                    .homeTeam(home)
                    .awayTeam(away)
                    .status(MatchStatus.SCHEDULED)
                    .startsAt(matchTime)
                    .build());
        }

        return matches;
    }

    /**
     * Busca un horario compatible entre dos equipos, considerando su disponibilidad semanal.
     * Si no hay coincidencia, genera un horario por defecto dentro del rango del torneo.
     * También valida que los equipos no tengan conflictos de horario con partidos ya generados.
     */
    private LocalDateTime findCompatibleSlot(Team teamA, Team teamB, Tournament tournament, List<Match> existingMatches) {
        // Primero intentar encontrar horario compatible basado en disponibilidad
        List<TeamAvailability> aAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamA.getId());
        List<TeamAvailability> bAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamB.getId());

        for (TeamAvailability a : aAvail) {
            for (TeamAvailability b : bAvail) {
                if (a.getDayOfWeek() == b.getDayOfWeek()
                        && a.getStartTime().equals(b.getStartTime())
                        && a.getEndTime().equals(b.getEndTime())) {

                    LocalDateTime candidateTime = calculateNextMatchTime(a.getDayOfWeek(), a.getStartTime(), tournament);
                    
                    // Verificar que no haya conflicto con partidos ya generados
                    if (!hasScheduleConflict(teamA, teamB, candidateTime, existingMatches)) {
                        return candidateTime;
                    }
                }
            }
        }

        // Si no hay coincidencia de disponibilidad, generar horario por defecto
        // Usar horario predeterminado: Lunes a Viernes a las 14:00, dentro del rango del torneo
        return generateDefaultMatchTime(tournament, existingMatches, teamA, teamB);
    }

    /**
     * Calcula la próxima fecha y hora para un partido basado en el día de la semana y hora.
     */
    private LocalDateTime calculateNextMatchTime(DayOfWeek dayOfWeek, LocalTime startTime, Tournament tournament) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = tournament.getStartDate();
        
        // Si la fecha de inicio del torneo es futura, usar esa como base
        LocalDate baseDate = startDate.isAfter(now.toLocalDate()) ? startDate : now.toLocalDate();
        LocalDateTime baseDateTime = baseDate.atTime(startTime);
        
        int todayValue = baseDateTime.getDayOfWeek().getValue();
        int matchDayValue = dayOfWeek.getValue();
        int daysUntilMatch = (matchDayValue - todayValue + 7) % 7;
        
        if (daysUntilMatch == 0) {
            daysUntilMatch = 7; // siguiente semana
        }
        
        LocalDateTime candidateTime = baseDateTime.plusDays(daysUntilMatch);
        
        // Asegurar que esté dentro del rango del torneo
        if (candidateTime.toLocalDate().isAfter(tournament.getEndDate())) {
            // Si se pasa del final, usar el primer día del torneo
            candidateTime = tournament.getStartDate().atTime(startTime);
        }
        
        return candidateTime.withSecond(0).withNano(0);
    }

    /**
     * Genera un horario por defecto cuando no hay coincidencia de disponibilidad.
     * Distribuye los partidos en diferentes días y horas para evitar conflictos.
     */
    private LocalDateTime generateDefaultMatchTime(Tournament tournament, List<Match> existingMatches, Team teamA, Team teamB) {
        LocalDate startDate = tournament.getStartDate();
        LocalDate endDate = tournament.getEndDate();
        
        // Horario por defecto: Lunes a Viernes, de 14:00 a 16:00, con intervalos de 2 horas
        LocalTime[] timeSlots = {LocalTime.of(14, 0), LocalTime.of(16, 0)};
        
        LocalDate currentDate = startDate.isAfter(LocalDate.now()) ? startDate : LocalDate.now();
        
        // Intentar encontrar un horario sin conflicto
        for (int dayOffset = 0; dayOffset <= 30; dayOffset++) {
            LocalDate candidateDate = currentDate.plusDays(dayOffset);
            
            // Verificar que esté dentro del rango del torneo
            if (candidateDate.isAfter(endDate)) {
                break;
            }
            
            DayOfWeek dayOfWeek = candidateDate.getDayOfWeek();
            
            // Solo considerar días laborables (Lunes a Viernes)
            if (dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue() && 
                dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue()) {
                
                for (LocalTime timeSlot : timeSlots) {
                    LocalDateTime candidateTime = candidateDate.atTime(timeSlot);
                    
                    if (!hasScheduleConflict(teamA, teamB, candidateTime, existingMatches)) {
                        return candidateTime;
                    }
                }
            }
        }
        
        // Si no se encuentra ningún horario sin conflicto, usar el primer día del torneo
        return tournament.getStartDate().atTime(LocalTime.of(14, 0));
    }

    /**
     * Verifica si un equipo tiene conflicto de horario (ya tiene otro partido a la misma hora y día).
     */
    private boolean hasScheduleConflict(Team teamA, Team teamB, LocalDateTime matchTime, List<Match> existingMatches) {
        // Verificar conflictos para ambos equipos
        for (Match match : existingMatches) {
            LocalDateTime existingTime = match.getStartsAt();
            if (existingTime == null) continue;
            
            // Verificar si es el mismo día y hora (con margen de 2 horas para considerar duración del partido)
            boolean sameDay = existingTime.toLocalDate().equals(matchTime.toLocalDate());
            boolean timeOverlap = Math.abs(java.time.Duration.between(existingTime, matchTime).toHours()) < 2;
            
            if (sameDay && timeOverlap) {
                // Verificar si alguno de los equipos ya tiene partido a esa hora
                if (match.getHomeTeam().getId().equals(teamA.getId()) || 
                    match.getAwayTeam().getId().equals(teamA.getId()) ||
                    match.getHomeTeam().getId().equals(teamB.getId()) || 
                    match.getAwayTeam().getId().equals(teamB.getId())) {
                    return true; // Hay conflicto
                }
            }
        }
        
        return false; // No hay conflicto
    }

    /**
     * Elimina todos los partidos del fixture de una categoría en un torneo.
     */
    @Transactional
    public void deleteFixture(Long tournamentId, Long categoryId) {
        List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        if (!matches.isEmpty()) {
            matchRepository.deleteAll(matches);
        }
    }
}