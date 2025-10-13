package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "standings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_standing", columnNames = {"tournament_id", "category_id", "team_id"})
        },
        indexes = {
                @Index(name = "idx_standing_tournament", columnList = "tournament_id"),
                @Index(name = "idx_standing_category", columnList = "category_id"),
                @Index(name = "idx_standing_points", columnList = "points DESC")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tournament", "category", "team"})
@EqualsAndHashCode(exclude = {"tournament", "category", "team"})
public class Standing implements Serializable {

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

    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Min(value = 0, message = "Points cannot be negative")
    @Column(nullable = false)
    private Integer points = 0;

    @Min(value = 0, message = "Played cannot be negative")
    @Column(nullable = false)
    private Integer played = 0;

    @Min(value = 0, message = "Wins cannot be negative")
    @Column(nullable = false)
    private Integer wins = 0;

    @Min(value = 0, message = "Draws cannot be negative")
    @Column(nullable = false)
    private Integer draws = 0;

    @Min(value = 0, message = "Losses cannot be negative")
    @Column(nullable = false)
    private Integer losses = 0;

    @Min(value = 0, message = "Goals for cannot be negative")
    @Column(name = "goals_for", nullable = false)
    private Integer goalsFor = 0;

    @Min(value = 0, message = "Goals against cannot be negative")
    @Column(name = "goals_against", nullable = false)
    private Integer goalsAgainst = 0;

    @Transient
    public Integer getGoalDifference() {
        return goalsFor - goalsAgainst;
    }
}