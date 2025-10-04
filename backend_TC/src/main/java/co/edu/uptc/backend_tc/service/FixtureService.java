package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.repository.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class FixtureService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final CategoryRepository categoryRepository;
    private final TeamAvailabilityRepository availabilityRepository;

    public FixtureService(TeamRepository teamRepository,
                          MatchRepository matchRepository,
                          TournamentRepository tournamentRepository,
                          CategoryRepository categoryRepository,
                          TeamAvailabilityRepository availabilityRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.categoryRepository = categoryRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public List<Match> generateRoundRobin(Long tournamentId, Long categoryId, LocalDateTime startDate) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 🔥 Obtener equipos activos
        List<Team> teams = teamRepository.findAll().stream()
                .filter(t -> t.getTournament().getId().equals(tournamentId))
                .filter(t -> t.getCategory().getId().equals(categoryId))
                .filter(Team::getIsActive)
                .toList();

        if (teams.size() < 2) {
            throw new RuntimeException("Not enough teams to generate fixtures");
        }

        List<Team> teamList = new ArrayList<>(teams);

        // Si es impar, añadimos un BYE
        if (teamList.size() % 2 != 0) {
            teamList.add(null);
        }

        int numTeams = teamList.size();
        int rounds = numTeams - 1;
        int matchesPerRound = numTeams / 2;

        List<Match> allMatches = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                Team home = teamList.get(i);
                Team away = teamList.get(numTeams - 1 - i);

                if (home != null && away != null) {
                    LocalDateTime slot = findNextAvailableSlot(home, away, startDate.plusDays(round));
                    Match match = Match.builder()
                            .tournament(tournament)
                            .category(category)
                            .homeTeam(home)
                            .awayTeam(away)
                            .startsAt(slot)
                            .status(slot != null ? co.edu.uptc.backend_tc.model.MatchStatus.SCHEDULED
                                    : co.edu.uptc.backend_tc.model.MatchStatus.POSTPONED)
                            .build();
                    allMatches.add(match);
                }
            }

            // Rotación (Round-Robin)
            List<Team> rotated = new ArrayList<>(teamList);
            Team fixed = rotated.remove(0);
            Collections.rotate(rotated, 1);
            rotated.add(0, fixed);
            teamList = rotated;
        }

        return matchRepository.saveAll(allMatches);
    }

    private LocalDateTime findNextAvailableSlot(Team home, Team away, LocalDateTime startDate) {
        List<TeamAvailability> homeSlots = availabilityRepository.findByTeamId(home.getId());
        List<TeamAvailability> awaySlots = availabilityRepository.findByTeamId(away.getId());

        for (int i = 0; i < 14; i++) { // buscar hasta 2 semanas
            LocalDateTime candidate = startDate.plusDays(i);
            DayOfWeek day = candidate.getDayOfWeek();

            List<TeamAvailability> homeDay = homeSlots.stream().filter(a -> a.getDayOfWeek().equals(day)).toList();
            List<TeamAvailability> awayDay = awaySlots.stream().filter(a -> a.getDayOfWeek().equals(day)).toList();

            for (TeamAvailability h : homeDay) {
                for (TeamAvailability a : awayDay) {
                    LocalTime start = h.getStartTime().isAfter(a.getStartTime()) ? h.getStartTime() : a.getStartTime();
                    LocalTime end = h.getEndTime().isBefore(a.getEndTime()) ? h.getEndTime() : a.getEndTime();

                    if (start.isBefore(end)) {
                        return LocalDateTime.of(candidate.toLocalDate(), start);
                    }
                }
            }
        }
        return null; // no coincidencia
    }
}
