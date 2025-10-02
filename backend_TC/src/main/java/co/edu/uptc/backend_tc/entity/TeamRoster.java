package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.entity.id.TeamRosterId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team_roster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TeamRosterId.class)
public class TeamRoster {
    @Id
    @ManyToOne @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Id
    @ManyToOne @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Short jerseyNumber;
    private Boolean isCaptain = false;
}
