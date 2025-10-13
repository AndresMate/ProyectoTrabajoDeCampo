package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.SanctionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanctions", indexes = {
        @Index(name = "idx_sanction_player", columnList = "player_id"),
        @Index(name = "idx_sanction_team", columnList = "team_id"),
        @Index(name = "idx_sanction_match", columnList = "match_id"),
        @Index(name = "idx_sanction_date", columnList = "date_issued"),
        @Index(name = "idx_sanction_valid", columnList = "valid_until")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"player", "team", "match"})
@EqualsAndHashCode(exclude = {"player", "team", "match"})
public class Sanction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @NotNull(message = "Sanction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SanctionType type;

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    @Column(length = 1000)
    private String reason;

    @Column(name = "date_issued", nullable = false, updatable = false)
    private LocalDateTime dateIssued;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @PrePersist
    protected void onCreate() {
        this.dateIssued = LocalDateTime.now();
        validateSanctionTarget();
    }

    @PreUpdate
    protected void onUpdate() {
        validateSanctionTarget();
    }

    private void validateSanctionTarget() {
        if (player == null && team == null) {
            throw new IllegalStateException("Sanction must be assigned to either a player or a team");
        }
    }

    @Transient
    public boolean isActive() {
        if (validUntil == null) return true;
        return LocalDateTime.now().isBefore(validUntil);
    }
}