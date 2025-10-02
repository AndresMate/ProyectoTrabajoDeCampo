package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer maxTeams;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.PLANNING;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @ManyToOne @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @ManyToOne @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
