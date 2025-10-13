package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.entity.id.InscriptionPlayerId;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "inscription_players", indexes = {
        @Index(name = "idx_inscription_player", columnList = "inscription_id,player_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(InscriptionPlayerId.class)
@ToString(exclude = {"inscription", "player"})
@EqualsAndHashCode(exclude = {"inscription", "player"})
public class InscriptionPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "Inscription is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscription_id", nullable = false)
    private Inscription inscription;

    @Id
    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
}