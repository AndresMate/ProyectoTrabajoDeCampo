package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"match"})
@EqualsAndHashCode(exclude = {"match"})
public class MatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long matchId;

    @NotNull(message = "Match is required")
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "match_id")
    private Match match;

    @Min(value = 0, message = "Home score cannot be negative")
    @Column(name = "home_score", nullable = false)
    private Integer homeScore = 0;

    @Min(value = 0, message = "Away score cannot be negative")
    @Column(name = "away_score", nullable = false)
    private Integer awayScore = 0;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @Column(name = "entered_at", nullable = false, updatable = false)
    private LocalDateTime enteredAt;

    @PrePersist
    protected void onCreate() {
        this.enteredAt = LocalDateTime.now();
    }
}