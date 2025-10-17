package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_sport", columnList = "sport_id"),
        @Index(name = "idx_category_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sport", "inscriptions", "teams", "matches", "standings"})
@EqualsAndHashCode(exclude = {"sport", "inscriptions", "teams", "matches", "standings"})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Members per team is required")
    @Min(value = 1, message = "Members per team must be at least 1")
    @Column(name = "members_per_team", nullable = false)
    private Integer membersPerTeam;

    @Column(nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Sport is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Inscription> inscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Match> matches = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Standing> standings = new ArrayList<>();

    public static Category withId(Long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
