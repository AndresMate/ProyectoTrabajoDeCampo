package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import co.edu.uptc.backend_tc.validation.OnCreate;
import co.edu.uptc.backend_tc.validation.OnUpdate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tournament name is required", groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 5, max = 200, message = "Name must be between 5 and 200 characters", groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, length = 200)
    private String name;

    @Positive(message = "Maximum teams must be positive", groups = {OnCreate.class, OnUpdate.class})
    @Column(name = "max_teams")
    private Integer maxTeams;

    @NotNull(message = "Start date is required", groups = {OnCreate.class, OnUpdate.class})
    @Future(message = "Start date must be in the future", groups = OnCreate.class)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required", groups = {OnCreate.class, OnUpdate.class})
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "inscription_start_date")
    private LocalDate inscriptionStartDate;

    @Column(name = "inscription_end_date")
    private LocalDate inscriptionEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TournamentStatus status = TournamentStatus.PLANNING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @NotNull(message = "Category is required", groups = {OnCreate.class, OnUpdate.class})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Sport is required", groups = {OnCreate.class, OnUpdate.class})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @NotNull(message = "Creator is required", groups = OnCreate.class)
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
            throw new IllegalStateException(
                    "La fecha de fin del torneo debe ser posterior o igual a la fecha de inicio. La fecha de fin (" + 
                    endDate.toString() + ") no puede ser anterior a la fecha de inicio (" + startDate.toString() + ")"
            );
        }
        
        // Validar orden de fechas de inscripción
        if (inscriptionStartDate != null && inscriptionEndDate != null 
                && inscriptionEndDate.isBefore(inscriptionStartDate)) {
            throw new IllegalStateException("Inscription end date cannot be before inscription start date");
        }
        
        // Validar que fechas de inscripción sean anteriores al inicio del torneo
        if (inscriptionEndDate != null && startDate != null 
                && !inscriptionEndDate.isBefore(startDate)) {
            throw new IllegalStateException("Inscription end date must be before tournament start date");
        }
    }
}