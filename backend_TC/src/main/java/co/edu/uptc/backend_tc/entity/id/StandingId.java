package co.edu.uptc.backend_tc.entity.id;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingId implements Serializable {
    private Long tournament;
    private Long category;
    private Long team;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandingId)) return false;
        StandingId that = (StandingId) o;
        return Objects.equals(tournament, that.tournament) &&
                Objects.equals(category, that.category) &&
                Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tournament, category, team);
    }
}
