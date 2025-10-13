package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Ejemplo de relación: un club tiene muchos miembros
    // @OneToMany(mappedBy = "club")
    // private List<Member> members;
}
