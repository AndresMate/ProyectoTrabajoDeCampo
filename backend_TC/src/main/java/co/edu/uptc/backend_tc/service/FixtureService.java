package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        // Verificar torneo
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", tournamentId));

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

                LocalDateTime matchTime = findCompatibleSlot(home, away);

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

            LocalDateTime matchTime = findCompatibleSlot(home, away);

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
     */
    private LocalDateTime findCompatibleSlot(Team teamA, Team teamB) {
        List<TeamAvailability> aAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamA.getId());
        List<TeamAvailability> bAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamB.getId());

        for (TeamAvailability a : aAvail) {
            for (TeamAvailability b : bAvail) {
                if (a.getDayOfWeek() == b.getDayOfWeek()
                        && a.getStartTime().equals(b.getStartTime())
                        && a.getEndTime().equals(b.getEndTime())) {

                    LocalDateTime now = LocalDateTime.now();
                    int today = now.getDayOfWeek().getValue();
                    int matchDay = a.getDayOfWeek().getValue();
                    int daysUntilMatch = (matchDay - today + 7) % 7;
                    if (daysUntilMatch == 0) daysUntilMatch = 7; // siguiente semana

                    return now.plusDays(daysUntilMatch)
                            .withHour(a.getStartTime().getHour())
                            .withMinute(a.getStartTime().getMinute())
                            .withSecond(0)
                            .withNano(0);
                }
            }
        }

        throw new BusinessException(
                String.format("No hay horarios compatibles entre %s y %s", teamA.getName(), teamB.getName()),
                "NO_COMPATIBLE_TIME_SLOT"
        );
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