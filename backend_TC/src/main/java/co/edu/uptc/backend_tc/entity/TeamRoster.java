package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.entity.id.TeamRosterId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "team_roster", indexes = {
        @Index(name = "idx_roster_team_player", columnList = "team_id,player_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TeamRosterId.class)
@ToString(exclude = {"team", "player"})
@EqualsAndHashCode(exclude = {"team", "player"})
public class TeamRoster implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Id
    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Min(value = 0, message = "Jersey number cannot be negative")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Builder.Default
    @Column(name = "is_captain", nullable = false)
    private Boolean isCaptain = false;
}
