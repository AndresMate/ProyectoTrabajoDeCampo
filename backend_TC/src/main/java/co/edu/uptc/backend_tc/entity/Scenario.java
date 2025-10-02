package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer capacity;
    private Boolean supportsNightGames = false;

    @ManyToOne @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
}
