package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResult {
    @Id
    @OneToOne @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private Integer homeScore = 0;
    private Integer awayScore = 0;
    private String notes;
    private OffsetDateTime enteredAt = OffsetDateTime.now();
    private OffsetDateTime validatedAt;

    @ManyToOne @JoinColumn(name = "entered_by", nullable = false)
    private User enteredBy;

    @ManyToOne @JoinColumn(name = "validated_by")
    private User validatedBy;
}
