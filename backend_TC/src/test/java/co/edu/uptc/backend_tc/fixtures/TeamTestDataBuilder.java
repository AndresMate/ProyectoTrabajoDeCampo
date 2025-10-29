package co.edu.uptc.backend_tc.fixtures;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.InscriptionStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Data Builder para crear instancias de Team con datos de prueba
 */
public class TeamTestDataBuilder {
    
    private Long id = 1L;
    private String name = "Equipo Test";
    private Boolean isActive = true;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    private Tournament tournament;
    private Club club;
    private List<Player> players = new ArrayList<>();
    private List<Inscription> inscriptions = new ArrayList<>();
    private List<TeamAvailability> availabilities = new ArrayList<>();
    private List<TeamRoster> rosters = new ArrayList<>();
    
    public TeamTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public TeamTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public TeamTestDataBuilder withIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    public TeamTestDataBuilder withCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public TeamTestDataBuilder withUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public TeamTestDataBuilder withTournament(Tournament tournament) {
        this.tournament = tournament;
        return this;
    }
    
    public TeamTestDataBuilder withClub(Club club) {
        this.club = club;
        return this;
    }
    
    public TeamTestDataBuilder withPlayers(List<Player> players) {
        this.players = players;
        return this;
    }
    
    public TeamTestDataBuilder withInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions;
        return this;
    }
    
    public TeamTestDataBuilder withAvailabilities(List<TeamAvailability> availabilities) {
        this.availabilities = availabilities;
        return this;
    }
    
    public TeamTestDataBuilder withRosters(List<TeamRoster> rosters) {
        this.rosters = rosters;
        return this;
    }
    
    public Team build() {
        return Team.builder()
                .id(id)
                .name(name)
                .isActive(isActive)
                .tournament(tournament)
                .club(club)
                .build();
    }
    
    // Métodos de conveniencia para casos comunes
    public static TeamTestDataBuilder aValidTeam() {
        return new TeamTestDataBuilder();
    }
    
    public static TeamTestDataBuilder anInactiveTeam() {
        return new TeamTestDataBuilder()
                .withIsActive(false);
    }
    
    public static TeamTestDataBuilder aTeamWithPlayers(List<Player> players) {
        return new TeamTestDataBuilder()
                .withPlayers(players);
    }
}
