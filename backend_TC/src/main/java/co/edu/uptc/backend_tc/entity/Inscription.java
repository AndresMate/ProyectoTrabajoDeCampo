package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscriptions", indexes = {
        @Index(name = "idx_inscription_tournament", columnList = "tournament_id"),
        @Index(name = "idx_inscription_status", columnList = "status"),
        @Index(name = "idx_inscription_category", columnList = "category_id"),
        @Index(name = "idx_inscription_delegate", columnList = "delegate_player_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tournament", "category", "delegate", "club", "players", "originatedTeam"})
@EqualsAndHashCode(exclude = {"tournament", "category", "delegate", "club", "players", "originatedTeam"})
public class Inscription implements Serializable {

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

    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    @Column(name = "team_name", nullable = false, length = 150)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InscriptionStatus status = InscriptionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_player_id")
    private Player delegate;

    @Column(nullable = false, length = 100)
    private String delegateName;

    @Column(nullable = false, length = 100)
    private String delegateEmail;

    @Column(nullable = false, length = 20)
    private String delegatePhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamAvailability> availability = new ArrayList<>();



    @Column(length = 500)
    private String rejectionReason;

    // Relación con jugadores inscritos
    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InscriptionPlayer> players;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @OneToOne(mappedBy = "originInscription")
    private Team originatedTeam;


}