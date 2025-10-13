package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios", indexes = {
        @Index(name = "idx_scenario_venue", columnList = "venue_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"venue", "matches"})
@EqualsAndHashCode(exclude = {"venue", "matches"})
public class Scenario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Scenario name is required")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @Positive(message = "Capacity must be positive")
    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "supports_night_games", nullable = false)
    private Boolean supportsNightGames = false;

    @NotNull(message = "Venue is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @OneToMany(mappedBy = "scenario")
    @Builder.Default
    private List<Match> matches = new ArrayList<>();
}