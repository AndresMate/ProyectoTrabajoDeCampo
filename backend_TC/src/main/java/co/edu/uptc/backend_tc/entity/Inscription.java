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

    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    @Column(name = "team_name", nullable = false, length = 150)
    private String teamName;

    @NotBlank(message = "Delegate phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    @Column(name = "delegate_phone", nullable = false, length = 20)
    private String delegatePhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InscriptionStatus status = InscriptionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @NotNull(message = "Tournament is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @NotNull(message = "Category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull(message = "Delegate player is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_player_id", nullable = false)
    private Player delegate;

    @NotNull(message = "Club is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InscriptionPlayer> players = new ArrayList<>();

    @OneToOne(mappedBy = "originInscription")
    private Team originatedTeam;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}