package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    private Boolean isActive = true;
}
