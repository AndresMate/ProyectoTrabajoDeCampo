package co.edu.uptc.backend_tc.fixtures;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Data Builder para crear instancias de Tournament con datos de prueba
 * Sigue el patrón Builder para facilitar la creación de objetos de prueba
 */
public class TournamentTestDataBuilder {
    
    private Long id = 1L;
    private String name = "Torneo Test";
    private Integer maxTeams = 8;
    private LocalDate startDate = LocalDate.now().plusDays(30);
    private LocalDate endDate = LocalDate.now().plusDays(60);
    private Modality modality = Modality.DIURNO;
    private TournamentStatus status = TournamentStatus.PLANNING;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private Category category;
    private Sport sport;
    private User createdBy;
    private List<Inscription> inscriptions = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<Match> matches = new ArrayList<>();
    private List<Standing> standings = new ArrayList<>();
    
    public TournamentTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public TournamentTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public TournamentTestDataBuilder withMaxTeams(Integer maxTeams) {
        this.maxTeams = maxTeams;
        return this;
    }
    
    public TournamentTestDataBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }
    
    public TournamentTestDataBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }
    
    public TournamentTestDataBuilder withModality(Modality modality) {
        this.modality = modality;
        return this;
    }
    
    public TournamentTestDataBuilder withStatus(TournamentStatus status) {
        this.status = status;
        return this;
    }
    
    public TournamentTestDataBuilder withCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public TournamentTestDataBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }
    
    public TournamentTestDataBuilder withSport(Sport sport) {
        this.sport = sport;
        return this;
    }
    
    public TournamentTestDataBuilder withCreatedBy(User createdBy) {
        this.createdBy = createdBy;
        return this;
    }
    
    public TournamentTestDataBuilder withInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions;
        return this;
    }
    
    public TournamentTestDataBuilder withTeams(List<Team> teams) {
        this.teams = teams;
        return this;
    }
    
    public TournamentTestDataBuilder withMatches(List<Match> matches) {
        this.matches = matches;
        return this;
    }
    
    public TournamentTestDataBuilder withStandings(List<Standing> standings) {
        this.standings = standings;
        return this;
    }
    
    public Tournament build() {
        return Tournament.builder()
                .id(id)
                .name(name)
                .maxTeams(maxTeams)
                .startDate(startDate)
                .endDate(endDate)
                .modality(modality)
                .status(status)
                .createdAt(createdAt)
                .category(category)
                .sport(sport)
                .createdBy(createdBy)
                .inscriptions(inscriptions)
                .teams(teams)
                .matches(matches)
                .standings(standings)
                .build();
    }
    
    // Métodos de conveniencia para casos comunes
    public static TournamentTestDataBuilder aValidTournament() {
        return new TournamentTestDataBuilder()
                .withSport(TournamentFixtures.validSport())
                .withCategory(TournamentFixtures.validCategory());
    }
    
    public static TournamentTestDataBuilder anInProgressTournament() {
        return new TournamentTestDataBuilder()
                .withStatus(TournamentStatus.IN_PROGRESS)
                .withSport(TournamentFixtures.validSport())
                .withCategory(TournamentFixtures.validCategory());
    }
    
    public static TournamentTestDataBuilder aFinishedTournament() {
        return new TournamentTestDataBuilder()
                .withStatus(TournamentStatus.FINISHED)
                .withSport(TournamentFixtures.validSport())
                .withCategory(TournamentFixtures.validCategory());
    }
    
    public static TournamentTestDataBuilder aTournamentWithOpenInscriptions() {
        return new TournamentTestDataBuilder()
                .withStatus(TournamentStatus.OPEN_FOR_INSCRIPTION)
                .withSport(TournamentFixtures.validSport())
                .withCategory(TournamentFixtures.validCategory());
    }
    
    public static TournamentTestDataBuilder aTournamentWithInvalidDates() {
        return new TournamentTestDataBuilder()
                .withStartDate(LocalDate.now().plusDays(60))
                .withEndDate(LocalDate.now().plusDays(30));
    }
}
