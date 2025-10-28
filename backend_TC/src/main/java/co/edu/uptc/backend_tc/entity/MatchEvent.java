package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.MatchEventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_events", indexes = {
        @Index(name = "idx_event_match", columnList = "match_id"),
        @Index(name = "idx_event_player", columnList = "player_id"),
        @Index(name = "idx_event_type", columnList = "type"),
        @Index(name = "idx_event_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"match", "player"})
@EqualsAndHashCode(exclude = {"match", "player"})
public class MatchEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Match is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchEventType type;

    @Min(value = 0, message = "Minute cannot be negative")
    @Max(value = 200, message = "Minute cannot exceed 200")
    @Column(name = "event_minute")
    private Integer minute;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}