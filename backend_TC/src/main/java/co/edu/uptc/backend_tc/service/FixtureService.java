package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FixtureService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamAvailabilityRepository availabilityRepository;

    public FixtureService(TournamentRepository tournamentRepository,
                          TeamRepository teamRepository,
                          MatchRepository matchRepository,
                          TeamAvailabilityRepository availabilityRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.availabilityRepository = availabilityRepository;
    }

    /**
     * Genera el fixture automáticamente según el modo elegido.
     * @param tournamentId id del torneo
     * @param categoryId id de la categoría
     * @param mode "round_robin" o "knockout"
     */
    @Transactional
    public void generateFixture(Long tournamentId, Long categoryId, String mode) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // 🔹 Filtrar equipos con inscripción aprobada
        List<Team> teams = teamRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId)
                .stream()
                .filter(t -> t.getOriginInscription() != null &&
                        t.getOriginInscription().getStatus() == InscriptionStatus.APPROVED)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new RuntimeException("Not enough approved teams to generate a fixture");
        }

        // 🔹 Elegir modo
        switch (mode.toLowerCase()) {
            case "round_robin" -> generateRoundRobin(tournament, categoryId, teams);
            case "knockout" -> generateKnockout(tournament, categoryId, teams);
            default -> throw new RuntimeException("Invalid fixture mode: " + mode);
        }
    }

    /**
     * Genera un fixture tipo "todos contra todos"
     */
    private void generateRoundRobin(Tournament tournament, Long categoryId, List<Team> teams) {
        int numTeams = teams.size();
        boolean hasBye = (numTeams % 2 != 0);

        if (hasBye) teams.add(null); // “bye” para número impar

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
                        .category(new Category(categoryId))
                        .homeTeam(home)
                        .awayTeam(away)
                        .status(MatchStatus.SCHEDULED)
                        .startsAt(matchTime)
                        .build();

                matches.add(match);
            }

            // rotar equipos excepto el primero
            teams.add(1, teams.remove(teams.size() - 1));
        }

        matchRepository.saveAll(matches);
    }

    /**
     * Genera un fixture tipo "eliminación directa"
     */
    private void generateKnockout(Tournament tournament, Long categoryId, List<Team> teams) {
        Collections.shuffle(teams); // emparejamientos aleatorios
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            if (i + 1 >= teams.size()) break; // impar queda libre

            Team home = teams.get(i);
            Team away = teams.get(i + 1);

            LocalDateTime matchTime = findCompatibleSlot(home, away);

            Match match = Match.builder()
                    .tournament(tournament)
                    .category(new Category(categoryId))
                    .homeTeam(home)
                    .awayTeam(away)
                    .status(MatchStatus.SCHEDULED)
                    .startsAt(matchTime)
                    .build();

            matches.add(match);
        }

        matchRepository.saveAll(matches);
    }

    /**
     * Busca un horario compatible entre dos equipos según TeamAvailability
     */
    private LocalDateTime findCompatibleSlot(Team teamA, Team teamB) {
        List<TeamAvailability> aAvail = availabilityRepository.findByTeamId(teamA.getId());
        List<TeamAvailability> bAvail = availabilityRepository.findByTeamId(teamB.getId());

        for (TeamAvailability a : aAvail) {
            for (TeamAvailability b : bAvail) {
                if (a.getDayOfWeek() == b.getDayOfWeek() &&
                        a.getStartTime().equals(b.getStartTime()) &&
                        a.getEndTime().equals(b.getEndTime())) {

                    // 🔹 Retorna un horario compatible
                    return LocalDateTime.now()
                            .with(a.getDayOfWeek())
                            .withHour(a.getStartTime().getHour())
                            .withMinute(a.getStartTime().getMinute());
                }
            }
        }

        throw new RuntimeException("No compatible time found between teams " +
                teamA.getName() + " and " + teamB.getName());
    }

    /**
     * Elimina todos los partidos de un torneo y categoría
     */
    @Transactional
    public void deleteFixture(Long tournamentId, Long categoryId) {
        List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        if (!matches.isEmpty()) {
            matchRepository.deleteAll(matches);
        }
    }
}
