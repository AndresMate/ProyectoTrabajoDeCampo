package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.MatchEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "match_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchEventType type; // Ej: GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION

    @Column(nullable = false)
    private Integer minute; // minuto del evento

    private String description; // opcional, detalles
}
