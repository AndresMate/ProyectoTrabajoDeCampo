package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean isActive = true;

    @ManyToOne @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToOne @JoinColumn(name = "origin_inscription_id")
    private Inscription originInscription;
}
