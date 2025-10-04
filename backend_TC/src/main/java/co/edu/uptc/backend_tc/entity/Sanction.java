package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.SanctionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sanctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con jugador (opcional si es para un equipo completo)
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    // Relación con equipo
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    // Relación con partido donde ocurrió la sanción
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SanctionType type; // Ej: YELLOW_CARD, RED_CARD, SUSPENSION, FINE

    private String reason;

    @Column(nullable = false)
    private LocalDateTime dateIssued = LocalDateTime.now();

    private LocalDateTime validUntil; // Ejemplo: suspensión hasta X fecha
}
