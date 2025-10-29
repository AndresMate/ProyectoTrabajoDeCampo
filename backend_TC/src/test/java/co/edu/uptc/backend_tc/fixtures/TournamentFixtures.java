package co.edu.uptc.backend_tc.fixtures;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixtures estáticas para crear objetos de prueba comunes
 * Proporciona métodos estáticos para crear instancias predefinidas de entidades
 */
public class TournamentFixtures {
    
    // ========== TOURNAMENT FIXTURES ==========
    
    public static Tournament validTournament() {
        return TournamentTestDataBuilder.aValidTournament().build();
    }
    
    public static Tournament inProgressTournament() {
        return TournamentTestDataBuilder.anInProgressTournament().build();
    }
    
    public static Tournament finishedTournament() {
        return TournamentTestDataBuilder.aFinishedTournament().build();
    }
    
    public static Tournament tournamentWithOpenInscriptions() {
        return TournamentTestDataBuilder.aTournamentWithOpenInscriptions().build();
    }
    
    public static Tournament tournamentWithInvalidDates() {
        return TournamentTestDataBuilder.aTournamentWithInvalidDates().build();
    }
    
    public static Tournament tournamentWithMaxTeams(Integer maxTeams) {
        return TournamentTestDataBuilder.aValidTournament()
                .withMaxTeams(maxTeams)
                .build();
    }
    
    public static Tournament diurnoTournament() {
        return TournamentTestDataBuilder.aValidTournament()
                .withModality(Modality.DIURNO)
                .build();
    }
    
    public static Tournament nocturnoTournament() {
        return TournamentTestDataBuilder.aValidTournament()
                .withModality(Modality.NOCTURNO)
                .build();
    }
    
    // ========== USER FIXTURES ==========
    
    public static User validUser() {
        return UserTestDataBuilder.aValidUser().build();
    }
    
    public static User adminUser() {
        return UserTestDataBuilder.anAdminUser().build();
    }
    
    public static User superAdminUser() {
        return UserTestDataBuilder.aSuperAdminUser().build();
    }
    
    public static User refereeUser() {
        return UserTestDataBuilder.aRefereeUser().build();
    }
    
    public static User playerUser() {
        return UserTestDataBuilder.aPlayerUser().build();
    }
    
    public static User inactiveUser() {
        return UserTestDataBuilder.anInactiveUser().build();
    }
    
    // ========== TEAM FIXTURES ==========
    
    public static Team validTeam() {
        return TeamTestDataBuilder.aValidTeam().build();
    }
    
    public static Team inactiveTeam() {
        return TeamTestDataBuilder.anInactiveTeam().build();
    }
    
    public static Team teamWithName(String name) {
        return TeamTestDataBuilder.aValidTeam()
                .withName(name)
                .build();
    }
    
    // ========== SPORT FIXTURES ==========
    
    public static Sport validSport() {
        return Sport.builder()
                .id(1L)
                .name("Fútbol")
                .description("Deporte de equipo con balón")
                .isActive(true)
                .build();
    }
    
    public static Sport basketballSport() {
        return Sport.builder()
                .id(2L)
                .name("Básquetbol")
                .description("Deporte de equipo con canasta")
                .isActive(true)
                .build();
    }
    
    // ========== CATEGORY FIXTURES ==========
    
    public static Category validCategory() {
        return Category.builder()
                .id(1L)
                .name("Sub-20")
                .description("Categoría juvenil")
                .membersPerTeam(11)
                .isActive(true)
                .sport(validSport())
                .build();
    }
    
    public static Category seniorCategory() {
        return Category.builder()
                .id(2L)
                .name("Senior")
                .description("Categoría adulta")
                .membersPerTeam(11)
                .isActive(true)
                .sport(validSport())
                .build();
    }
    
    // ========== CLUB FIXTURES ==========
    
    public static Club validClub() {
        return Club.builder()
                .id(1L)
                .name("Club Test")
                .description("Club de prueba")
                .isActive(true)
                .build();
    }
    
    // ========== VENUE FIXTURES ==========
    
    public static Venue validVenue() {
        return Venue.builder()
                .id(1L)
                .name("Cancha Principal")
                .address("Campus Principal UPTC")
                .build();
    }
    
    // ========== SCENARIO FIXTURES ==========
    
    public static Scenario validScenario() {
        return Scenario.builder()
                .id(1L)
                .name("Escenario Principal")
                .capacity(100)
                .supportsNightGames(true)
                .build();
    }
    
    // ========== PLAYER FIXTURES ==========
    
    public static Player validPlayer() {
        return Player.builder()
                .id(1L)
                .fullName("Juan Pérez")
                .institutionalEmail("juan.perez@uptc.edu.co")
                .documentNumber("12345678")
                .studentCode("2024001")
                .birthDate(LocalDate.now().minusYears(20))
                .isActive(true)
                .build();
    }
    
    public static Player playerWithAge(int age) {
        return Player.builder()
                .id(1L)
                .fullName("Juan Pérez")
                .institutionalEmail("juan.perez@uptc.edu.co")
                .documentNumber("12345678")
                .studentCode("2024001")
                .birthDate(LocalDate.now().minusYears(age))
                .isActive(true)
                .build();
    }
    
    // ========== INSCRIPTION FIXTURES ==========
    
    public static Inscription validInscription() {
        return Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
    }
    
    public static Inscription approvedInscription() {
        return Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.APPROVED)
                .createdAt(OffsetDateTime.now())
                .build();
    }
    
    public static Inscription rejectedInscription() {
        return Inscription.builder()
                .id(1L)
                .status(InscriptionStatus.REJECTED)
                .createdAt(OffsetDateTime.now())
                .build();
    }
    
    // ========== MATCH FIXTURES ==========
    
    public static Match validMatch() {
        return Match.builder()
                .id(1L)
                .startsAt(LocalDateTime.now().plusDays(1))
                .build();
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Crea una lista de torneos para pruebas
     */
    public static List<Tournament> createTournamentList(int count) {
        List<Tournament> tournaments = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            tournaments.add(TournamentTestDataBuilder.aValidTournament()
                    .withId((long) i)
                    .withName("Torneo " + i)
                    .build());
        }
        return tournaments;
    }
    
    /**
     * Crea una lista de usuarios para pruebas
     */
    public static List<User> createUserList(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(UserTestDataBuilder.aValidUser()
                    .withId((long) i)
                    .withEmail("user" + i + "@uptc.edu.co")
                    .build());
        }
        return users;
    }
    
    /**
     * Crea una lista de equipos para pruebas
     */
    public static List<Team> createTeamList(int count) {
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            teams.add(TeamTestDataBuilder.aValidTeam()
                    .withId((long) i)
                    .withName("Equipo " + i)
                    .build());
        }
        return teams;
    }
}
