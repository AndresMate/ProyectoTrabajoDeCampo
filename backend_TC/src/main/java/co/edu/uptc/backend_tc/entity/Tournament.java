package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments", indexes = {
        @Index(name = "idx_tournament_dates", columnList = "start_date,end_date"),
        @Index(name = "idx_tournament_status", columnList = "status"),
        @Index(name = "idx_tournament_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"category", "sport", "createdBy", "inscriptions", "teams", "matches", "standings"})
@EqualsAndHashCode(exclude = {"category", "sport", "createdBy", "inscriptions", "teams", "matches", "standings"})
public class Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tournament name is required")
    @Size(min = 5, max = 200, message = "Name must be between 5 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @Positive(message = "Maximum teams must be positive")
    @Column(name = "max_teams")
    private Integer maxTeams;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TournamentStatus status = TournamentStatus.PLANNING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // ✅ Nueva relación con Category
    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Sport is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "tournament")
    @Builder.Default
    private List<Inscription> inscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "tournament")
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "tournament")
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    @OneToMany(mappedBy = "tournament")
    @Builder.Default
    private List<Standing> standings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void validateDates() {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalStateException("End date cannot be before start date");
        }
    }
}
