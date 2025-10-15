package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", indexes = {
        @Index(name = "idx_team_tournament", columnList = "tournament_id"),
        @Index(name = "idx_team_category", columnList = "category_id"),
        @Index(name = "idx_team_club", columnList = "club_id"),
        @Index(name = "idx_team_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tournament", "category", "originInscription", "club", "availabilities", "roster", "homeMatches", "awayMatches", "standings", "sanctions"})
@EqualsAndHashCode(exclude = {"tournament", "category", "originInscription", "club", "availabilities", "roster", "homeMatches", "awayMatches", "standings", "sanctions"})
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Tournament is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_inscription_id")
    private Inscription originInscription;

    @NotNull(message = "Club is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToMany(mappedBy = "team", orphanRemoval = true)
    @Builder.Default
    private List<TeamAvailability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "team", orphanRemoval = true)
    @Builder.Default
    private List<TeamRoster> roster = new ArrayList<>();

    @OneToMany(mappedBy = "homeTeam")
    @Builder.Default
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "awayTeam")
    @Builder.Default
    private List<Match> awayMatches = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private List<Standing> standings = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private List<Sanction> sanctions = new ArrayList<>();
}