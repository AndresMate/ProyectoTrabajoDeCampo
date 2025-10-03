package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "standings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    private Integer points = 0;
    private Integer played = 0;
    private Integer wins = 0;
    private Integer draws = 0;
    private Integer losses = 0;
    private Integer goalsFor = 0;
    private Integer goalsAgainst = 0;
}
