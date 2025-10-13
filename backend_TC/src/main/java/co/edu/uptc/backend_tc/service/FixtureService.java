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

    @Transactional
    public void generateFixture(Long tournamentId, Long categoryId, String mode) {
        // Verificar torneo
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", tournamentId));

        // Verificar categoría
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // Filtrar equipos con inscripción aprobada
        List<Team> teams = teamRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId)
                .stream()
                .filter(t -> t.getOriginInscription() != null &&
                        t.getOriginInscription().getStatus() == InscriptionStatus.APPROVED)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new BusinessException(
                    "Not enough approved teams to generate a fixture (minimum 2)",
                    "INSUFFICIENT_TEAMS"
            );
        }

        // Eliminar fixture existente si hay
        deleteFixture(tournamentId, categoryId);

        // Elegir modo
        switch (mode.toLowerCase()) {
            case "round_robin" -> generateRoundRobin(tournament, category, teams);
            case "knockout" -> generateKnockout(tournament, category, teams);
            default -> throw new BadRequestException("Invalid fixture mode: " + mode + ". Use 'round_robin' or 'knockout'");
        }
    }

    private void generateRoundRobin(Tournament tournament, Category category, List<Team> teams) {
        int numTeams = teams.size();
        boolean hasBye = (numTeams % 2 != 0);

        if (hasBye) teams.add(null); // "bye" para número impar

        int rounds = teams.size() - 1;
        int matchesPerRound = teams.size() / 2;

        List<Match> matches = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                Team home = teams.get(i);
                Team away = teams.get(teams.size() - 1 - i);

                if (home == null || away == null) continue; // uno descansa

                LocalDateTime matchTime = findCompatibleSlot(home, away);

                Match match = Match.builder()
                        .tournament(tournament)
                        .category(category)
                        .homeTeam(home)
                        .awayTeam(away)
                        .status(MatchStatus.SCHEDULED)
                        .startsAt(matchTime)
                        .build();

                matches.add(match);
            }

            // Rotar equipos excepto el primero
            teams.add(1, teams.remove(teams.size() - 1));
        }

        matchRepository.saveAll(matches);
    }

    private void generateKnockout(Tournament tournament, Category category, List<Team> teams) {
        Collections.shuffle(teams); // emparejamientos aleatorios
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            if (i + 1 >= teams.size()) break; // impar queda libre

            Team home = teams.get(i);
            Team away = teams.get(i + 1);

            LocalDateTime matchTime = findCompatibleSlot(home, away);

            Match match = Match.builder()
                    .tournament(tournament)
                    .category(category)
                    .homeTeam(home)
                    .awayTeam(away)
                    .status(MatchStatus.SCHEDULED)
                    .startsAt(matchTime)
                    .build();

            matches.add(match);
        }

        matchRepository.saveAll(matches);
    }

    private LocalDateTime findCompatibleSlot(Team teamA, Team teamB) {
        List<TeamAvailability> aAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamA.getId());
        List<TeamAvailability> bAvail = availabilityRepository.findByTeamIdAndAvailableTrue(teamB.getId());

        for (TeamAvailability a : aAvail) {
            for (TeamAvailability b : bAvail) {
                if (a.getDayOfWeek() == b.getDayOfWeek() &&
                        a.getStartTime().equals(b.getStartTime()) &&
                        a.getEndTime().equals(b.getEndTime())) {

                    // Retorna un horario compatible
                    return LocalDateTime.now()
                            .with(a.getDayOfWeek())
                            .withHour(a.getStartTime().getHour())
                            .withMinute(a.getStartTime().getMinute())
                            .withSecond(0)
                            .withNano(0);
                }
            }
        }

        throw new BusinessException(
                String.format("No compatible time found between teams %s and %s",
                        teamA.getName(), teamB.getName()),
                "NO_COMPATIBLE_TIME_SLOT"
        );
    }

    @Transactional
    public void deleteFixture(Long tournamentId, Long categoryId) {
        List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        if (!matches.isEmpty()) {
            matchRepository.deleteAll(matches);
        }
    }
}
