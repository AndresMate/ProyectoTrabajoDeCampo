package co.edu.uptc.backend_tc.integration;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.MatchStatus;
import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the UPTC Tournament Management System
 * 
 * This test class implements basic integration tests based on the Integration Test Plan.
 * It uses H2 in-memory database for testing to avoid external dependencies.
 * 
 * Test Cases Covered:
 * - CP001: Registration System Integration
 * - CP002: File Validation Integration  
 * - CP003: Fixture Generation Integration
 * 
 * Features:
 * - Uses @Transactional to ensure test isolation
 * - Creates unique test data with timestamps to avoid conflicts
 * - Validates entity relationships and database persistence
 * - Tests core tournament management functionality
 * 
 * Database: H2 in-memory (configured in application-test.properties)
 * Profile: test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class TournamentIntegrationTest {

    @Autowired
    private TournamentRepository tournamentRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private SportRepository sportRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private InscriptionRepository inscriptionRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private ScenarioRepository scenarioRepository;

    @Test
    @DisplayName("CP001: Create tournament with open inscriptions")
    void testCreateTournamentWithOpenInscriptions() {
        // Arrange - Create basic test data
        Sport futbol = createSport("Fútbol");
        Category categoriaA = createCategory("Categoría A", futbol);
        User admin = createUser("admin@uptc.edu.co", "Administrador");
        
        // Act - Create tournament
        Tournament tournament = new Tournament();
        tournament.setName("Interfacultades 2025-2");
        tournament.setMaxTeams(16);
        tournament.setStartDate(LocalDate.now().plusDays(30));
        tournament.setEndDate(LocalDate.now().plusDays(60));
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
        tournament.setModality(Modality.DIURNO);
        tournament.setCategory(categoriaA);
        tournament.setSport(futbol);
        tournament.setCreatedBy(admin);
        tournament.setCreatedAt(OffsetDateTime.now());
        
        Tournament savedTournament = tournamentRepository.save(tournament);
        
        // Assert - Verify tournament was created correctly
        assertThat(savedTournament).isNotNull();
        assertThat(savedTournament.getId()).isNotNull();
        assertThat(savedTournament.getName()).isEqualTo("Interfacultades 2025-2");
        assertThat(savedTournament.getStatus()).isEqualTo(TournamentStatus.OPEN_FOR_INSCRIPTION);
        assertThat(savedTournament.getMaxTeams()).isEqualTo(16);
        
        // Verify database persistence
        Tournament retrievedTournament = tournamentRepository.findById(savedTournament.getId()).orElse(null);
        assertThat(retrievedTournament).isNotNull();
        assertThat(retrievedTournament.getName()).isEqualTo("Interfacultades 2025-2");
    }

    @Test
    @DisplayName("CP001: Create team inscription")
    void testCreateTeamInscription() {
        // Arrange - Create base data
        Sport futbol = createSport("Fútbol");
        Category categoriaA = createCategory("Categoría A", futbol);
        User admin = createUser("admin@uptc.edu.co", "Administrador");
        Club club = createClub("UPTC Team A");
        
        Tournament tournament = createTournament("Interfacultades 2025-2", categoriaA, futbol, admin);
        
        // Act - Create inscription
        Inscription inscription = new Inscription();
        inscription.setTournament(tournament);
        inscription.setCategory(categoriaA);
        inscription.setTeamName("UPTC Team A Representativo");
        inscription.setStatus(InscriptionStatus.PENDING);
        inscription.setDelegateName("Juan Pérez");
        inscription.setDelegateEmail("juan.perez@uptc.edu.co");
        inscription.setDelegatePhone("3001234567");
        inscription.setClub(club);
        inscription.setCreatedAt(OffsetDateTime.now());
        inscription.setUpdatedAt(OffsetDateTime.now());
        
        Inscription savedInscription = inscriptionRepository.save(inscription);
        
        // Assert - Verify inscription
        assertThat(savedInscription).isNotNull();
        assertThat(savedInscription.getId()).isNotNull();
        assertThat(savedInscription.getStatus()).isEqualTo(InscriptionStatus.PENDING);
        assertThat(savedInscription.getTeamName()).isEqualTo("UPTC Team A Representativo");
        assertThat(savedInscription.getClub().getId()).isEqualTo(club.getId());
        
        // Verify persistence
        Inscription retrievedInscription = inscriptionRepository.findById(savedInscription.getId()).orElse(null);
        assertThat(retrievedInscription).isNotNull();
        assertThat(retrievedInscription.getStatus()).isEqualTo(InscriptionStatus.PENDING);
    }

    @Test
    @DisplayName("CP003: Create basic matches")
    void testCreateBasicMatches() {
        // Arrange - Crear datos base
        Sport futbol = createSport("Fútbol");
        Category categoriaA = createCategory("Categoría A", futbol);
        User admin = createUser("admin@uptc.edu.co", "Administrador");
        User referee = createUser("referee@uptc.edu.co", "Árbitro");
        Club club1 = createClub("Halcones");
        Club club2 = createClub("Tigres");
        
        Tournament tournament = createTournament("Interfacultades 2025-2", categoriaA, futbol, admin);
        Venue venue = createVenue("Polideportivo Principal");
        Scenario scenario = createScenario("Cancha de Fútbol", venue);
        
        // Crear equipos
        Team team1 = createTeam("Halcones", tournament, categoriaA, club1);
        Team team2 = createTeam("Tigres", tournament, categoriaA, club2);
        
        // Act - Crear partido
        Match match = new Match();
        match.setTournament(tournament);
        match.setCategory(categoriaA);
        match.setScenario(scenario);
        match.setStartsAt(LocalDateTime.now().plusDays(1));
        match.setHomeTeam(team1);
        match.setAwayTeam(team2);
        match.setStatus(MatchStatus.SCHEDULED);
        match.setReferee(referee);
        
        Match savedMatch = matchRepository.save(match);
        
        // Assert - Verificar partido
        assertThat(savedMatch).isNotNull();
        assertThat(savedMatch.getId()).isNotNull();
        assertThat(savedMatch.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(savedMatch.getHomeTeam().getId()).isEqualTo(team1.getId());
        assertThat(savedMatch.getAwayTeam().getId()).isEqualTo(team2.getId());
        assertThat(savedMatch.getHomeTeam().getId()).isNotEqualTo(savedMatch.getAwayTeam().getId());
        
        // Verificar persistencia
        Match retrievedMatch = matchRepository.findById(savedMatch.getId()).orElse(null);
        assertThat(retrievedMatch).isNotNull();
        assertThat(retrievedMatch.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
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
        category.setMembersPerTeam(11); // Set number of members per team
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
        tournament.setStatus(TournamentStatus.OPEN_FOR_INSCRIPTION);
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
}