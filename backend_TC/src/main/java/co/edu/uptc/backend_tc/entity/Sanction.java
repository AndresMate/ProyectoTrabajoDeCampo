package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sanctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sanction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private OffsetDateTime appliedAt = OffsetDateTime.now();

    @ManyToOne @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne @JoinColumn(name = "match_id")
    private Match match;
}
