package co.edu.uptc.backend_tc.entity;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;
    private String delegatePhone;

    @Enumerated(EnumType.STRING)
    private InscriptionStatus status = InscriptionStatus.PENDING;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @ManyToOne @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne @JoinColumn(name = "delegate_player_id", nullable = false)
    private Player delegate;

    @ManyToOne @JoinColumn(name = "club_id")
    private Club club; // opcional según modalidad
}
