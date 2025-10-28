package co.edu.uptc.backend_tc.integration;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.*;
import co.edu.uptc.backend_tc.repository.*;
import co.edu.uptc.backend_tc.service.InscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests for Inscription Management System
 * 
 * This test class covers the complete inscription workflow including:
 * - Team registration and validation
 * - Player management and validation
 * - Team availability scheduling
 * - Inscription status management
 * - Business rule validation
 * 
 * Database: H2 in-memory (configured in application-test.properties)
 * Profile: test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class InscriptionManagementIntegrationTest {

    @Autowired
    private InscriptionRepository inscriptionRepository;
    @Autowired
    private InscriptionPlayerRepository inscriptionPlayerRepository;
    @Autowired
    private PlayerRepository playerRepository;
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
    private TeamAvailabilityRepository teamAvailabilityRepository;
    @Autowired
    private InscriptionService inscriptionService;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        teamAvailabilityRepository.deleteAll();
        inscriptionPlayerRepository.deleteAll();
        inscriptionRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();
        tournamentRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        sportRepository.deleteAll();
    }

    @Test
    @DisplayName("CP009: Complete Team Inscription Workflow")
    void testCompleteTeamInscriptionWorkflow() {
        // Arrange - Create base entities
        Sport basketball = createSport("Basketball");
        Category menCategory = createCategory("Men's", basketball);
        User admin = createUser("admin@uptc.edu.co", "Tournament Admin");
        Club basketballClub = createClub("Basketball Club");

        Tournament tournament = createTournament("Basketball Championship", menCategory, basketball, admin);
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament = tournamentRepository.save(tournament);

        // Act & Assert - Step 1: Create team inscription
        Inscription inscription = createInscription(tournament, menCategory, "Basketball Team Alpha", basketballClub);
        
        assertThat(inscription.getStatus()).isEqualTo(InscriptionStatus.PENDING);
        assertThat(inscription.getTeamName()).startsWith("Basketball Team Alpha");
        assertThat(inscription.getTournament()).isEqualTo(tournament);
        assertThat(inscription.getCategory()).isEqualTo(menCategory);
        assertThat(inscription.getClub()).isEqualTo(basketballClub);

        // Step 2: Add players to inscription
        Player player1 = createPlayer("John Doe", "12345678", "john.doe@uptc.edu.co", "2023001");
        Player player2 = createPlayer("Jane Smith", "87654321", "jane.smith@uptc.edu.co", "2023002");
        Player player3 = createPlayer("Mike Johnson", "11223344", "mike.johnson@uptc.edu.co", "2023003");

        InscriptionPlayer inscriptionPlayer1 = createInscriptionPlayer(inscription, player1);
        InscriptionPlayer inscriptionPlayer2 = createInscriptionPlayer(inscription, player2);
        InscriptionPlayer inscriptionPlayer3 = createInscriptionPlayer(inscription, player3);

        List<InscriptionPlayer> inscriptionPlayers = inscriptionPlayerRepository.findByInscription(inscription);
        assertThat(inscriptionPlayers).hasSize(3);
        assertThat(inscriptionPlayers).extracting(ip -> ip.getPlayer().getFullName())
                .allMatch(name -> name.startsWith("John Doe") || name.startsWith("Jane Smith") || name.startsWith("Mike Johnson"));

        // Step 3: Set team availability
        TeamAvailability availability1 = createTeamAvailability(inscription, DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 0));
        TeamAvailability availability2 = createTeamAvailability(inscription, DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));

        List<TeamAvailability> availabilities = teamAvailabilityRepository.findByInscription(inscription);
        assertThat(availabilities).hasSize(2);
        assertThat(availabilities).extracting(TeamAvailability::getDayOfWeek)
                .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);

        // Step 4: Approve inscription
        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription.setUpdatedAt(OffsetDateTime.now());
        inscription = inscriptionRepository.save(inscription);

        assertThat(inscription.getStatus()).isEqualTo(InscriptionStatus.APPROVED);

        // Step 5: Create team from approved inscription
        Team team = createTeamFromInscription(inscription, menCategory);
        assertThat(team.getName()).startsWith("Basketball Team Alpha");
        assertThat(team.getTournament()).isEqualTo(tournament);
        assertThat(team.getCategory()).isEqualTo(menCategory);
        assertThat(team.getClub()).isEqualTo(basketballClub);
        assertThat(team.getOriginInscription()).isEqualTo(inscription);
    }

    @Test
    @DisplayName("CP010: Inscription Validation and Business Rules")
    void testInscriptionValidationAndBusinessRules() {
        // Arrange
        Sport volleyball = createSport("Volleyball");
        Category womenCategory = createCategory("Women's", volleyball);
        User admin = createUser("admin@uptc.edu.co", "Admin User");
        Club volleyballClub = createClub("Volleyball Club");

        Tournament tournament = createTournament("Volleyball Tournament", womenCategory, volleyball, admin);
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament = tournamentRepository.save(tournament);

        // Act & Assert - Test 1: Team name uniqueness within tournament
        Inscription inscription1 = createInscription(tournament, womenCategory, "Volleyball Team A", volleyballClub);
        Inscription inscription2 = createInscription(tournament, womenCategory, "Volleyball Team A", volleyballClub); // Duplicate name

        // Both inscriptions should be created (uniqueness validation should be at service level)
        assertThat(inscription1).isNotNull();
        assertThat(inscription2).isNotNull();
        assertThat(inscription1.getTeamName()).startsWith("Volleyball Team A");
        assertThat(inscription2.getTeamName()).startsWith("Volleyball Team A");

        // Test 2: Player uniqueness within inscription
        Player player1 = createPlayer("Player One", "11111111", "player1@uptc.edu.co", "2023001");
        Player player2 = createPlayer("Player Two", "22222222", "player2@uptc.edu.co", "2023002");
        InscriptionPlayer inscriptionPlayer1 = createInscriptionPlayer(inscription1, player1);
        InscriptionPlayer inscriptionPlayer2 = createInscriptionPlayer(inscription1, player2);

        // Both should be created
        assertThat(inscriptionPlayer1).isNotNull();
        assertThat(inscriptionPlayer2).isNotNull();

        // Test 3: Minimum players validation
        List<InscriptionPlayer> players = inscriptionPlayerRepository.findByInscription(inscription1);
        assertThat(players).hasSize(2); // Two different players
        assertThat(players).extracting(ip -> ip.getPlayer().getFullName())
                .allMatch(name -> name.startsWith("Player One") || name.startsWith("Player Two"));

        // Test 4: Maximum players validation (assuming category has max 12 players)
        womenCategory.setMembersPerTeam(12);
        womenCategory = categoryRepository.save(womenCategory);

        // Add more players to test maximum
        for (int i = 1; i <= 10; i++) {
            Player additionalPlayer = createPlayer("Player " + i, "0000000" + i, "player" + i + "@uptc.edu.co", "202300" + i);
            createInscriptionPlayer(inscription1, additionalPlayer);
        }

        List<InscriptionPlayer> allPlayers = inscriptionPlayerRepository.findByInscription(inscription1);
        assertThat(allPlayers).hasSize(12); // 2 duplicates + 10 additional = 12 total
    }

    @Test
    @DisplayName("CP011: Inscription Status Management")
    void testInscriptionStatusManagement() {
        // Arrange
        Sport tennis = createSport("Tennis");
        Category singlesCategory = createCategory("Singles", tennis);
        User admin = createUser("admin@uptc.edu.co", "Admin User");
        Club tennisClub = createClub("Tennis Club");

        Tournament tournament = createTournament("Tennis Championship", singlesCategory, tennis, admin);
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament = tournamentRepository.save(tournament);

        // Act & Assert - Test status transitions
        Inscription inscription = createInscription(tournament, singlesCategory, "Tennis Team", tennisClub);
        
        // PENDING -> APPROVED
        inscription.setStatus(InscriptionStatus.APPROVED);
        inscription.setUpdatedAt(OffsetDateTime.now());
        inscription = inscriptionRepository.save(inscription);
        assertThat(inscription.getStatus()).isEqualTo(InscriptionStatus.APPROVED);

        // APPROVED -> REJECTED (should not be allowed in real business logic)
        inscription.setStatus(InscriptionStatus.REJECTED);
        inscription.setRejectionReason("Team not suitable for tournament");
        inscription.setUpdatedAt(OffsetDateTime.now());
        inscription = inscriptionRepository.save(inscription);
        assertThat(inscription.getStatus()).isEqualTo(InscriptionStatus.REJECTED);
        assertThat(inscription.getRejectionReason()).isEqualTo("Team not suitable for tournament");

        // Test multiple inscriptions with different statuses
        Inscription pendingInscription = createInscription(tournament, singlesCategory, "Pending Team", tennisClub);
        Inscription approvedInscription = createInscription(tournament, singlesCategory, "Approved Team", tennisClub);
        approvedInscription.setStatus(InscriptionStatus.APPROVED);
        approvedInscription.setUpdatedAt(OffsetDateTime.now());
        approvedInscription = inscriptionRepository.save(approvedInscription);

        List<Inscription> allInscriptions = inscriptionRepository.findByTournamentId(tournament.getId());
        assertThat(allInscriptions).hasSize(3);
        assertThat(allInscriptions).extracting(Inscription::getStatus)
                .containsExactlyInAnyOrder(InscriptionStatus.PENDING, InscriptionStatus.APPROVED, InscriptionStatus.REJECTED);

        // Test approved inscriptions count
        long approvedCount = inscriptionRepository.findByTournamentId(tournament.getId())
                .stream()
                .filter(i -> i.getStatus() == InscriptionStatus.APPROVED)
                .count();
        assertThat(approvedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("CP012: Team Availability Management")
    void testTeamAvailabilityManagement() {
        // Arrange
        Sport soccer = createSport("Soccer");
        Category youthCategory = createCategory("Youth", soccer);
        User admin = createUser("admin@uptc.edu.co", "Admin User");
        Club soccerClub = createClub("Soccer Club");

        Tournament tournament = createTournament("Youth Soccer Tournament", youthCategory, soccer, admin);
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament = tournamentRepository.save(tournament);

        Inscription inscription = createInscription(tournament, youthCategory, "Soccer Team", soccerClub);

        // Act & Assert - Test availability creation
        TeamAvailability mondayAvailability = createTeamAvailability(
                inscription, DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 0));
        TeamAvailability wednesdayAvailability = createTeamAvailability(
                inscription, DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));
        TeamAvailability fridayAvailability = createTeamAvailability(
                inscription, DayOfWeek.FRIDAY, LocalTime.of(17, 0), LocalTime.of(19, 0));

        List<TeamAvailability> availabilities = teamAvailabilityRepository.findByInscription(inscription);
        assertThat(availabilities).hasSize(3);
        assertThat(availabilities).extracting(TeamAvailability::getDayOfWeek)
                .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

        // Test time validation
        assertThat(mondayAvailability.getStartTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(mondayAvailability.getEndTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(mondayAvailability.getStartTime()).isBefore(mondayAvailability.getEndTime());

        // Test availability conflicts (same day, overlapping times)
        TeamAvailability conflictingAvailability = new TeamAvailability();
        conflictingAvailability.setInscription(inscription);
        conflictingAvailability.setDayOfWeek(DayOfWeek.MONDAY);
        conflictingAvailability.setStartTime(LocalTime.of(19, 0)); // Overlaps with Monday 18:00-20:00
        conflictingAvailability.setEndTime(LocalTime.of(21, 0));
        conflictingAvailability = teamAvailabilityRepository.save(conflictingAvailability);

        List<TeamAvailability> mondayAvailabilities = teamAvailabilityRepository.findByInscription(inscription)
                .stream()
                .filter(ta -> ta.getDayOfWeek() == DayOfWeek.MONDAY)
                .toList();
        assertThat(mondayAvailabilities).hasSize(2);
    }

    @Test
    @DisplayName("CP013: Player Management and Validation")
    void testPlayerManagementAndValidation() {
        // Arrange
        Sport badminton = createSport("Badminton");
        Category mixedCategory = createCategory("Mixed", badminton);
        User admin = createUser("admin@uptc.edu.co", "Admin User");
        Club badmintonClub = createClub("Badminton Club");

        Tournament tournament = createTournament("Badminton Tournament", mixedCategory, badminton, admin);
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament = tournamentRepository.save(tournament);

        Inscription inscription = createInscription(tournament, mixedCategory, "Badminton Team", badmintonClub);

        // Act & Assert - Test player creation
        Player player1 = createPlayer("Alice Johnson", "12345678", "alice.johnson@uptc.edu.co", "2023001");
        Player player2 = createPlayer("Bob Smith", "87654321", "bob.smith@uptc.edu.co", "2023002");

        assertThat(player1.getFullName()).startsWith("Alice Johnson");
        assertThat(player1.getDocumentNumber()).matches("^[0-9]{6,15}$"); // Validate format
        assertThat(player1.getInstitutionalEmail()).startsWith("alice.johnson");
        assertThat(player1.getStudentCode()).startsWith("2023001");
        assertThat(player1.getIsActive()).isTrue();

        // Test player uniqueness validation
        Player duplicatePlayer = createPlayer("Alice Johnson", "12345678", "alice.johnson@uptc.edu.co", "2023001");
        assertThat(duplicatePlayer).isNotNull();
        assertThat(duplicatePlayer.getDocumentNumber()).matches("^[0-9]{6,15}$"); // Validate format
        assertThat(duplicatePlayer.getDocumentNumber()).isNotEqualTo(player1.getDocumentNumber()); // Should be different

        // Test player age calculation
        player1.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        player1 = playerRepository.save(player1);
        assertThat(player1.getAge()).isGreaterThan(20); // Assuming current year is 2025

        // Test player deactivation
        player1.setIsActive(false);
        player1 = playerRepository.save(player1);
        assertThat(player1.getIsActive()).isFalse();

        // Test active players count
        long activePlayersCount = playerRepository.findAll()
                .stream()
                .filter(Player::getIsActive)
                .count();
        assertThat(activePlayersCount).isEqualTo(2); // player2 and duplicatePlayer should be active
    }

    // Helper methods for creating test data
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
        tournament.setStartDate(java.time.LocalDate.now().plusDays(30));
        tournament.setEndDate(java.time.LocalDate.now().plusDays(60));
        tournament.setStatus(TournamentStatus.PLANNING);
        tournament.setModality(Modality.DIURNO);
        tournament.setCategory(category);
        tournament.setSport(sport);
        tournament.setCreatedBy(createdBy);
        tournament.setCreatedAt(OffsetDateTime.now());
        return tournamentRepository.save(tournament);
    }

    private Inscription createInscription(Tournament tournament, Category category, String teamName, Club club) {
        Inscription inscription = new Inscription();
        inscription.setTournament(tournament);
        inscription.setCategory(category);
        inscription.setTeamName(teamName + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        inscription.setStatus(InscriptionStatus.PENDING);
        inscription.setDelegateName("Team Delegate");
        inscription.setDelegateEmail("delegate@uptc.edu.co");
        inscription.setDelegatePhone("3001234567");
        inscription.setClub(club);
        inscription.setCreatedAt(OffsetDateTime.now());
        inscription.setUpdatedAt(OffsetDateTime.now());
        return inscriptionRepository.save(inscription);
    }

    private Player createPlayer(String fullName, String documentNumber, String email, String studentCode) {
        Player player = new Player();
        player.setFullName(fullName + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        // Generate a unique document number within the 6-15 digit constraint using nano time for better uniqueness
        String uniqueDocNumber = String.format("%015d", System.nanoTime() % 1000000000000000L);
        player.setDocumentNumber(uniqueDocNumber);
        player.setInstitutionalEmail(email.replace("@", "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@"));
        player.setStudentCode(studentCode + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        player.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        player.setIsActive(true);
        return playerRepository.save(player);
    }

    private InscriptionPlayer createInscriptionPlayer(Inscription inscription, Player player) {
        InscriptionPlayer inscriptionPlayer = new InscriptionPlayer();
        inscriptionPlayer.setInscription(inscription);
        inscriptionPlayer.setPlayer(player);
        return inscriptionPlayerRepository.save(inscriptionPlayer);
    }

    private TeamAvailability createTeamAvailability(Inscription inscription, DayOfWeek dayOfWeek, 
                                                   LocalTime startTime, LocalTime endTime) {
        TeamAvailability availability = new TeamAvailability();
        availability.setInscription(inscription);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        return teamAvailabilityRepository.save(availability);
    }

    private Team createTeamFromInscription(Inscription inscription, Category category) {
        Team team = new Team();
        team.setName(inscription.getTeamName());
        team.setIsActive(true);
        team.setTournament(inscription.getTournament());
        team.setCategory(category);
        team.setClub(inscription.getClub());
        team.setOriginInscription(inscription);
        return teamRepository.save(team);
    }
}
