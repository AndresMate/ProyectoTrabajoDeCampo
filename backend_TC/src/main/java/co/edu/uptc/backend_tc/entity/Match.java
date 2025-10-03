package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.MatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    private LocalDateTime startsAt;

    @ManyToOne @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne @JoinColumn(name = "referee_id")
    private User referee;
}
