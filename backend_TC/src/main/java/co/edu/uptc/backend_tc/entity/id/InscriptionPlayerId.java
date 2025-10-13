package co.edu.uptc.backend_tc.entity.id;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionPlayerId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long inscription;
    private Long player;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InscriptionPlayerId that = (InscriptionPlayerId) o;
        return Objects.equals(inscription, that.inscription) &&
                Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inscription, player);
    }
}