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
    @OneToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private Integer homeScore = 0;
    private Integer awayScore = 0;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "entered_by", nullable = false)
    private User enteredBy;

    private LocalDateTime enteredAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    private LocalDateTime validatedAt;
}
