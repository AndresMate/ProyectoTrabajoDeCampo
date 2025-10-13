package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"scenarios"})
@EqualsAndHashCode(exclude = {"scenarios"})
public class Venue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Venue name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(length = 500)
    private String address;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Scenario> scenarios = new ArrayList<>();
}