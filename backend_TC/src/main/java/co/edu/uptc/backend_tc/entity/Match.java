package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.MatchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_match_tournament", columnList = "tournament_id"),
        @Index(name = "idx_match_category", columnList = "category_id"),
        @Index(name = "idx_match_date", columnList = "starts_at"),
        @Index(name = "idx_match_status", columnList = "status"),
        @Index(name = "idx_match_scenario", columnList = "scenario_id"),
        @Index(name = "idx_match_teams", columnList = "home_team_id,away_team_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tournament", "category", "scenario", "homeTeam", "awayTeam", "referee", "result", "events", "sanctions"})
@EqualsAndHashCode(exclude = {"tournament", "category", "scenario", "homeTeam", "awayTeam", "referee", "result", "events", "sanctions"})
public class Match implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tournament is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @NotNull(message = "Home team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @NotNull(message = "Away team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id")
    private User referee;

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private MatchResult result;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "match")
    @Builder.Default
    private List<Sanction> sanctions = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void validateTeams() {
        if (homeTeam != null && awayTeam != null && homeTeam.getId().equals(awayTeam.getId())) {
            throw new IllegalStateException("Home team and away team cannot be the same");
        }
    }
}