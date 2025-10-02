package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.entity.id.InscriptionPlayerId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inscription_players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(InscriptionPlayerId.class)
public class InscriptionPlayer {
    @Id
    @ManyToOne @JoinColumn(name = "inscription_id", nullable = false)
    private Inscription inscription;

    @Id
    @ManyToOne @JoinColumn(name = "player_id", nullable = false)
    private Player player;
}
