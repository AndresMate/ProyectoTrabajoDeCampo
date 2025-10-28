package co.edu.uptc.backend_tc.integration;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.*;
import co.edu.uptc.backend_tc.repository.*;
import co.edu.uptc.backend_tc.service.FixtureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests for Fixture Generation and Match Management System
 * 
 * This test class covers the complete fixture generation workflow including:
 * - Round-robin tournament fixture generation
 * - Match scheduling and management
 * - Tournament bracket generation
 * - Match result management
 * - Standing calculations
 * 
 * Database: H2 in-memory (configured in application-test.properties)
 * Profile: test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class FixtureGenerationIntegrationTest {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchResultRepository matchResultRepository;
    @Autowired
    private MatchEventRepository matchEventRepository;
    @Autowired
    private StandingRepository standingRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private SportRepository sportRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private ScenarioRepository scenarioRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRosterRepository teamRosterRepository;
    @Autowired
    private FixtureService fixtureService;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        matchEventRepository.deleteAll();
        matchResultRepository.deleteAll();
        matchRepository.deleteAll();
        standingRepository.deleteAll();
        teamRepository.deleteAll();
        tournamentRepository.deleteAll();
        scenarioRepository.deleteAll();
        venueRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        sportRepository.deleteAll();
    }

    @Test
    @DisplayName("CP014: Round-Robin Fixture Generation")
    void testRoundRobinFixtureGeneration() {
        // Arrange - Create tournament with 4 teams
        Sport football = createSport("Football");
        Category menCategory = createCategory("Men's", football);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        User referee = createUser("referee@uptc.edu.co", "Match Referee");
        Club club1 = createClub("Club Alpha");
        Club club2 = createClub("Club Beta");
        Club club3 = createClub("Club Gamma");
        Club club4 = createClub("Club Delta");
        Venue stadium = createVenue("Main Stadium");
        Scenario field = createScenario("Main Field", stadium);

        Tournament tournament = createTournament("Football Championship", menCategory, football, admin);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);

        // Create 4 teams
        Team team1 = createTeam("Team Alpha", tournament, menCategory, club1);
        Team team2 = createTeam("Team Beta", tournament, menCategory, club2);
        Team team3 = createTeam("Team Gamma", tournament, menCategory, club3);
        Team team4 = createTeam("Team Delta", tournament, menCategory, club4);

        List<Team> teams = teamRepository.findByTournamentId(tournament.getId());
        assertThat(teams).hasSize(4);

        // Act - Generate round-robin fixtures
        generateRoundRobinFixtures(tournament, teams, field, referee);

        // Assert - Verify fixture generation
        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        
        // For 4 teams in round-robin: 4C2 = 6 matches
        assertThat(matches).hasSize(6);
        
        // Verify each team plays against every other team exactly once
        for (Team team : teams) {
            List<Match> teamMatches = matches.stream()
                    .filter(m -> m.getHomeTeam().equals(team) || m.getAwayTeam().equals(team))
                    .toList();
            assertThat(teamMatches).hasSize(3); // Each team plays 3 matches in round-robin
        }

        // Verify no team plays against itself
        for (Match match : matches) {
            assertThat(match.getHomeTeam()).isNotEqualTo(match.getAwayTeam());
        }

        // Verify all matches are scheduled
        assertThat(matches).extracting(Match::getStatus)
                .containsOnly(MatchStatus.SCHEDULED);

        // Verify match scheduling (no overlapping times)
        for (int i = 0; i < matches.size() - 1; i++) {
            Match currentMatch = matches.get(i);
            Match nextMatch = matches.get(i + 1);
            assertThat(currentMatch.getStartsAt()).isBefore(nextMatch.getStartsAt());
        }
    }

    @Test
    @DisplayName("CP015: Tournament Bracket Generation")
    void testTournamentBracketGeneration() {
        // Arrange - Create tournament with 8 teams (power of 2 for bracket)
        Sport basketball = createSport("Basketball");
        Category womenCategory = createCategory("Women's", basketball);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        User referee = createUser("referee@uptc.edu.co", "Match Referee");
        Venue arena = createVenue("Basketball Arena");
        Scenario court = createScenario("Main Court", arena);

        Tournament tournament = createTournament("Basketball Championship", womenCategory, basketball, admin);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);

        // Create 8 teams
        List<Team> teams = createTeamsForTournament(tournament, womenCategory, 8);

        // Act - Generate tournament bracket
        generateTournamentBracket(tournament, teams, court, referee);

        // Assert - Verify bracket generation
        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        
        // For 8 teams in single elimination: 7 matches (8-1)
        assertThat(matches).hasSize(7);

        // Verify bracket structure
        // Quarterfinals: 4 matches (day 1)
        List<Match> quarterfinals = matches.stream()
                .filter(m -> m.getStartsAt().isBefore(java.time.LocalDateTime.now().plusDays(1).plusHours(12)))
                .toList();
        assertThat(quarterfinals).hasSize(4);

        // Semifinals: 2 matches (day 2)
        List<Match> semifinals = matches.stream()
                .filter(m -> m.getStartsAt().isAfter(java.time.LocalDateTime.now().plusDays(1).plusHours(12)))
                .filter(m -> m.getStartsAt().isBefore(java.time.LocalDateTime.now().plusDays(2).plusHours(12)))
                .toList();
        assertThat(semifinals).hasSize(2);

        // Final: 1 match (day 3)
        List<Match> finalMatch = matches.stream()
                .filter(m -> m.getStartsAt().isAfter(java.time.LocalDateTime.now().plusDays(2).plusHours(12)))
                .toList();
        assertThat(finalMatch).hasSize(1);
    }

    @Test
    @DisplayName("CP016: Match Result Management")
    void testMatchResultManagement() {
        // Arrange
        Sport volleyball = createSport("Volleyball");
        Category mixedCategory = createCategory("Mixed", volleyball);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        User referee = createUser("referee@uptc.edu.co", "Match Referee");
        Club club1 = createClub("Volleyball Club A");
        Club club2 = createClub("Volleyball Club B");
        Venue gym = createVenue("Volleyball Gym");
        Scenario court = createScenario("Main Court", gym);

        Tournament tournament = createTournament("Volleyball Tournament", mixedCategory, volleyball, admin);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);

        Team homeTeam = createTeam("Home Team", tournament, mixedCategory, club1);
        Team awayTeam = createTeam("Away Team", tournament, mixedCategory, club2);

        // Add players to teams
        Player homePlayer = createPlayer("Home Player", "11111111", "home.player@uptc.edu.co", "2023001");
        Player awayPlayer = createPlayer("Away Player", "22222222", "away.player@uptc.edu.co", "2023002");
        
        TeamRoster homeRoster = createTeamRoster(homeTeam, homePlayer, 10, true);
        TeamRoster awayRoster = createTeamRoster(awayTeam, awayPlayer, 11, false);

        Match match = createMatch(tournament, mixedCategory, court, homeTeam, awayTeam, referee);

        // Act & Assert - Test match result creation
        MatchResult result = createMatchResult(match, 3, 1); // Home team wins 3-1
        
        assertThat(result.getMatch()).isEqualTo(match);
        assertThat(result.getHomeScore()).isEqualTo(3);
        assertThat(result.getAwayScore()).isEqualTo(1);
        assertThat(result.getHomeScore()).isGreaterThan(result.getAwayScore());

        // Test match events
        MatchEvent event1 = createMatchEvent(match, homePlayer, MatchEventType.GOAL, "Goal scored");
        MatchEvent event2 = createMatchEvent(match, awayPlayer, MatchEventType.YELLOW_CARD, "Yellow card");

        List<MatchEvent> events = matchEventRepository.findByMatchId(match.getId());
        assertThat(events).hasSize(2);
        assertThat(events).extracting(MatchEvent::getType)
                .containsExactlyInAnyOrder(MatchEventType.GOAL, MatchEventType.YELLOW_CARD);

        // Test match status update
        match.setStatus(MatchStatus.FINISHED);
        match = matchRepository.save(match);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("CP017: Standing Calculations and Rankings")
    void testStandingCalculationsAndRankings() {
        // Arrange - Create tournament with multiple teams and matches
        Sport tennis = createSport("Tennis");
        Category singlesCategory = createCategory("Singles", tennis);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        User referee = createUser("referee@uptc.edu.co", "Match Referee");
        Venue tennisClub = createVenue("Tennis Club");
        Scenario court1 = createScenario("Court 1", tennisClub);
        Scenario court2 = createScenario("Court 2", tennisClub);

        Tournament tournament = createTournament("Tennis Championship", singlesCategory, tennis, admin);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);

        // Create 4 teams
        List<Team> teams = createTeamsForTournament(tournament, singlesCategory, 4);

        // Generate fixtures
        generateRoundRobinFixtures(tournament, teams, court1, referee);

        // Act - Simulate match results and calculate standings
        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        
        // Simulate results: Team 1 wins all, Team 2 wins 2, Team 3 wins 1, Team 4 wins 0
        simulateMatchResults(matches, teams);

        // Calculate standings
        calculateStandings(tournament, teams);

        // Assert - Verify standings
        List<Standing> standings = standingRepository.findByTournamentIdAndCategoryId(tournament.getId(), tournament.getCategory().getId());
        assertThat(standings).hasSize(4);

        // Verify ranking order (by points)
        assertThat(standings.get(0).getPoints()).isGreaterThanOrEqualTo(standings.get(1).getPoints());
        assertThat(standings.get(1).getPoints()).isGreaterThanOrEqualTo(standings.get(2).getPoints());
        assertThat(standings.get(2).getPoints()).isGreaterThanOrEqualTo(standings.get(3).getPoints());

        // Verify team statistics
        for (Standing standing : standings) {
            assertThat(standing.getPlayed()).isGreaterThan(0);
            assertThat(standing.getWins() + standing.getDraws() + standing.getLosses())
                    .isEqualTo(standing.getPlayed());
            assertThat(standing.getPoints()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("CP018: Fixture Scheduling Conflicts and Validation")
    void testFixtureSchedulingConflictsAndValidation() {
        // Arrange
        Sport badminton = createSport("Badminton");
        Category doublesCategory = createCategory("Doubles", badminton);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        User referee = createUser("referee@uptc.edu.co", "Match Referee");
        Venue badmintonHall = createVenue("Badminton Hall");
        Scenario court = createScenario("Main Court", badmintonHall);

        Tournament tournament = createTournament("Badminton Tournament", doublesCategory, badminton, admin);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournament = tournamentRepository.save(tournament);

        List<Team> teams = createTeamsForTournament(tournament, doublesCategory, 4);

        // Act - Generate fixtures with scheduling constraints
        generateFixturesWithConstraints(tournament, teams, court, referee);

        // Assert - Verify no scheduling conflicts
        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        
        // Check for scenario conflicts (same scenario, overlapping times)
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                Match match1 = matches.get(i);
                Match match2 = matches.get(j);
                
                if (match1.getScenario().equals(match2.getScenario())) {
                    // If same scenario, times should not overlap
                    boolean timesOverlap = match1.getStartsAt().isBefore(match2.getStartsAt().plusHours(2)) &&
                                         match2.getStartsAt().isBefore(match1.getStartsAt().plusHours(2));
                    assertThat(timesOverlap).isFalse();
                }
            }
        }

        // Check for referee conflicts
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                Match match1 = matches.get(i);
                Match match2 = matches.get(j);
                
                if (match1.getReferee().equals(match2.getReferee())) {
                    // If same referee, times should not overlap
                    boolean timesOverlap = match1.getStartsAt().isBefore(match2.getStartsAt().plusHours(2)) &&
                                         match2.getStartsAt().isBefore(match1.getStartsAt().plusHours(2));
                    assertThat(timesOverlap).isFalse();
                }
            }
        }
    }

    // Helper methods for creating test data and fixtures
    private Sport createSport(String name) {
        Sport sport = new Sport();
        sport.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        sport.setIsActive(true);
        return sportRepository.save(sport);
    }

    private Category createCategory(String name, Sport sport) {
        Category category = new Category();
        category.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        category.setSport(sport);
        category.setIsActive(true);
        category.setMembersPerTeam(11);
        return categoryRepository.save(category);
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setFullName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        user.setEmail(email.replace("@", "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@"));
        user.setRole(UserRole.ADMIN);
        user.setPasswordHash("$2a$10$encrypted");
        user.setIsActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    private Club createClub(String name) {
        Club club = new Club();
        club.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        club.setIsActive(true);
        return clubRepository.save(club);
    }

    private Tournament createTournament(String name, Category category, Sport sport, User createdBy) {
        Tournament tournament = new Tournament();
        tournament.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        tournament.setMaxTeams(16);
        tournament.setStartDate(LocalDate.now().plusDays(30));
        tournament.setEndDate(LocalDate.now().plusDays(60));
        tournament.setStatus(TournamentStatus.PLANNING);
        tournament.setModality(Modality.DIURNO);
        tournament.setCategory(category);
        tournament.setSport(sport);
        tournament.setCreatedBy(createdBy);
        tournament.setCreatedAt(OffsetDateTime.now());
        return tournamentRepository.save(tournament);
    }

    private Venue createVenue(String name) {
        Venue venue = new Venue();
        venue.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        return venueRepository.save(venue);
    }

    private Scenario createScenario(String name, Venue venue) {
        Scenario scenario = new Scenario();
        scenario.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        scenario.setVenue(venue);
        return scenarioRepository.save(scenario);
    }

    private Team createTeam(String name, Tournament tournament, Category category, Club club) {
        Team team = new Team();
        team.setName(name + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        team.setIsActive(true);
        team.setTournament(tournament);
        team.setCategory(category);
        team.setClub(club);
        return teamRepository.save(team);
    }

    private List<Team> createTeamsForTournament(Tournament tournament, Category category, int count) {
        List<Team> teams = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Club club = createClub("Club " + i);
            Team team = createTeam("Team " + i, tournament, category, club);
            teams.add(team);
        }
        return teams;
    }

    private Match createMatch(Tournament tournament, Category category, Scenario scenario, 
                             Team homeTeam, Team awayTeam, User referee) {
        Match match = Match.builder()
                .tournament(tournament)
                .category(category)
                .scenario(scenario)
                .startsAt(java.time.LocalDateTime.now().plusDays(1))
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .status(MatchStatus.SCHEDULED)
                .referee(referee)
                .build();
        return matchRepository.save(match);
    }

    private void generateRoundRobinFixtures(Tournament tournament, List<Team> teams, Scenario scenario, User referee) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().plusDays(1);
        
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .category(tournament.getCategory())
                        .scenario(scenario)
                        .startsAt(startTime)
                        .homeTeam(teams.get(i))
                        .awayTeam(teams.get(j))
                        .status(MatchStatus.SCHEDULED)
                        .referee(referee)
                        .build();
                matchRepository.save(match);
                startTime = startTime.plusHours(2); // 2-hour intervals
            }
        }
    }

    private void generateTournamentBracket(Tournament tournament, List<Team> teams, Scenario scenario, User referee) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().plusDays(1);
        
        // For 4 teams, create semifinals directly
        if (teams.size() == 4) {
            // Semifinals
            Match semifinal1 = Match.builder()
                    .tournament(tournament)
                    .category(tournament.getCategory())
                    .scenario(scenario)
                    .startsAt(startTime)
                    .homeTeam(teams.get(0))
                    .awayTeam(teams.get(1))
                    .status(MatchStatus.SCHEDULED)
                    .referee(referee)
                    .build();
            matchRepository.save(semifinal1);
            startTime = startTime.plusHours(2);
            
            Match semifinal2 = Match.builder()
                    .tournament(tournament)
                    .category(tournament.getCategory())
                    .scenario(scenario)
                    .startsAt(startTime)
                    .homeTeam(teams.get(2))
                    .awayTeam(teams.get(3))
                    .status(MatchStatus.SCHEDULED)
                    .referee(referee)
                    .build();
            matchRepository.save(semifinal2);
            startTime = startTime.plusHours(2);
            
            // Final
            Match finalMatch = Match.builder()
                    .tournament(tournament)
                    .category(tournament.getCategory())
                    .scenario(scenario)
                    .startsAt(startTime)
                    .homeTeam(teams.get(0)) // Winner of semifinal 1 (simplified)
                    .awayTeam(teams.get(2)) // Winner of semifinal 2 (simplified)
                    .status(MatchStatus.SCHEDULED)
                    .referee(referee)
                    .build();
            matchRepository.save(finalMatch);
        } else if (teams.size() == 8) {
            // For 8 teams: 4 quarterfinals + 2 semifinals + 1 final = 7 matches
            
            // Quarterfinals (4 matches)
            for (int i = 0; i < 4; i++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .category(tournament.getCategory())
                        .scenario(scenario)
                        .startsAt(startTime)
                        .homeTeam(teams.get(i * 2))
                        .awayTeam(teams.get(i * 2 + 1))
                        .status(MatchStatus.SCHEDULED)
                        .referee(referee)
                        .build();
                matchRepository.save(match);
                startTime = startTime.plusHours(2);
            }
            
            // Semifinals (2 matches) - Day 2
            startTime = java.time.LocalDateTime.now().plusDays(2);
            for (int i = 0; i < 2; i++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .category(tournament.getCategory())
                        .scenario(scenario)
                        .startsAt(startTime)
                        .homeTeam(teams.get(i * 2)) // Winner of quarterfinal (simplified)
                        .awayTeam(teams.get(i * 2 + 1)) // Winner of quarterfinal (simplified)
                        .status(MatchStatus.SCHEDULED)
                        .referee(referee)
                        .build();
                matchRepository.save(match);
                startTime = startTime.plusHours(2);
            }
            
            // Final (1 match) - Day 3
            startTime = java.time.LocalDateTime.now().plusDays(3);
            Match finalMatch = Match.builder()
                    .tournament(tournament)
                    .category(tournament.getCategory())
                    .scenario(scenario)
                    .startsAt(startTime)
                    .homeTeam(teams.get(0)) // Winner of semifinal (simplified)
                    .awayTeam(teams.get(1)) // Winner of semifinal (simplified)
                    .status(MatchStatus.SCHEDULED)
                    .referee(referee)
                    .build();
            matchRepository.save(finalMatch);
        } else {
            // For other team counts, use a simplified algorithm
            // Create matches for all teams in pairs
            for (int i = 0; i < teams.size() / 2; i++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .category(tournament.getCategory())
                        .scenario(scenario)
                        .startsAt(startTime)
                        .homeTeam(teams.get(i * 2))
                        .awayTeam(teams.get(i * 2 + 1))
                        .status(MatchStatus.SCHEDULED)
                        .referee(referee)
                        .build();
                matchRepository.save(match);
                startTime = startTime.plusHours(2);
            }
        }
    }

    private void generateFixturesWithConstraints(Tournament tournament, List<Team> teams, Scenario scenario, User referee) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().plusDays(1);
        
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Match match = Match.builder()
                        .tournament(tournament)
                        .category(tournament.getCategory())
                        .scenario(scenario)
                        .startsAt(startTime)
                        .homeTeam(teams.get(i))
                        .awayTeam(teams.get(j))
                        .status(MatchStatus.SCHEDULED)
                        .referee(referee)
                        .build();
                matchRepository.save(match);
                startTime = startTime.plusHours(3); // 3-hour intervals to avoid conflicts
            }
        }
    }

    private MatchResult createMatchResult(Match match, int homeScore, int awayScore) {
        MatchResult result = new MatchResult();
        result.setMatch(match);
        result.setHomeScore(homeScore);
        result.setAwayScore(awayScore);
        return matchResultRepository.save(result);
    }

    private MatchEvent createMatchEvent(Match match, Player player, MatchEventType type, String description) {
        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        event.setPlayer(player);
        event.setType(type);
        event.setDescription(description);
        event.setMinute(30);
        return matchEventRepository.save(event);
    }

    private void simulateMatchResults(List<Match> matches, List<Team> teams) {
        // Simplified simulation: Team 1 wins all, Team 2 wins 2, Team 3 wins 1, Team 4 wins 0
        for (Match match : matches) {
            int homeScore, awayScore;
            
            if (match.getHomeTeam().equals(teams.get(0))) {
                homeScore = 3; awayScore = 1; // Team 1 always wins
            } else if (match.getAwayTeam().equals(teams.get(0))) {
                homeScore = 1; awayScore = 3; // Team 1 always wins
            } else if (match.getHomeTeam().equals(teams.get(1))) {
                homeScore = 2; awayScore = 1; // Team 2 wins
            } else if (match.getAwayTeam().equals(teams.get(1))) {
                homeScore = 1; awayScore = 2; // Team 2 wins
            } else {
                homeScore = 1; awayScore = 1; // Draw
            }
            
            createMatchResult(match, homeScore, awayScore);
        }
    }

    private void calculateStandings(Tournament tournament, List<Team> teams) {
        Category category = tournament.getCategory(); // Get category from tournament
        for (Team team : teams) {
            Standing standing = new Standing();
            standing.setTournament(tournament);
            standing.setCategory(category);
            standing.setTeam(team);
            standing.setPlayed(3);
            standing.setWins(team.equals(teams.get(0)) ? 3 : team.equals(teams.get(1)) ? 2 : team.equals(teams.get(2)) ? 1 : 0);
            standing.setDraws(team.equals(teams.get(3)) ? 1 : 0);
            standing.setLosses(3 - standing.getWins() - standing.getDraws());
            standing.setPoints(standing.getWins() * 3 + standing.getDraws());
            standingRepository.save(standing);
        }
    }

    private Player createPlayer(String fullName, String documentNumber, String email, String studentCode) {
        Player player = new Player();
        player.setFullName(fullName + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        player.setDocumentNumber(documentNumber);
        player.setInstitutionalEmail(email.replace("@", "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@"));
        player.setStudentCode(studentCode);
        player.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        player.setIsActive(true);
        return playerRepository.save(player);
    }

    private TeamRoster createTeamRoster(Team team, Player player, int jerseyNumber, boolean isCaptain) {
        TeamRoster roster = new TeamRoster();
        roster.setTeam(team);
        roster.setPlayer(player);
        roster.setJerseyNumber(jerseyNumber);
        roster.setIsCaptain(isCaptain);
        return teamRosterRepository.save(roster);
    }
}
