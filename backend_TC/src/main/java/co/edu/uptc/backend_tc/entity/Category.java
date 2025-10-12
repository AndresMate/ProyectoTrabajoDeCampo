package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private Boolean isActive = true;

    @ManyToOne @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    public Category(Long id) {
        this.id = id;
    }

}
