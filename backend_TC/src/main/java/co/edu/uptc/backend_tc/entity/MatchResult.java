package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {

    @Id
    private Long matchId; // la PK es el mismo id del partido

    @OneToOne
    @MapsId
    @JoinColumn(name = "match_id")
    private Match match;

    private Integer homeScore = 0;
    private Integer awayScore = 0;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime enteredAt = LocalDateTime.now();
}
