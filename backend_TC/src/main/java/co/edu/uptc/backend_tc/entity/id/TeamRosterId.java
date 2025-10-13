package co.edu.uptc.backend_tc.entity.id;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRosterId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long team;
    private Long player;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamRosterId that = (TeamRosterId) o;
        return Objects.equals(team, that.team) &&
                Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, player);
    }
}