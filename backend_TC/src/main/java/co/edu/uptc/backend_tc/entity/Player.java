package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players", indexes = {
        @Index(name = "idx_player_document", columnList = "document_number"),
        @Index(name = "idx_player_email", columnList = "institutional_email"),
        @Index(name = "idx_player_code", columnList = "student_code"),
        @Index(name = "idx_player_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"documents", "inscriptionsAsDelegate", "inscriptionPlayers", "teamRosters", "matchEvents", "sanctions"})
@EqualsAndHashCode(exclude = {"documents", "inscriptionsAsDelegate", "inscriptionPlayers", "teamRosters", "matchEvents", "sanctions"})
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 200, message = "Full name must be between 3 and 200 characters")
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Size(max = 50)
    @Column(name = "student_code", unique = true, length = 50)
    private String studentCode;

    @NotBlank(message = "Document number is required")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Document number must be between 6 and 15 digits")
    @Column(name = "document_number", unique = true, nullable = false, length = 20)
    private String documentNumber;

    @Email(message = "Invalid institutional email format")
    @Column(name = "institutional_email", unique = true, length = 150)
    private String institutionalEmail;

    @Past(message = "Birth date must be in the past")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlayerDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "delegate")
    @Builder.Default
    private List<Inscription> inscriptionsAsDelegate = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InscriptionPlayer> inscriptionPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamRoster> teamRosters = new ArrayList<>();

    @OneToMany(mappedBy = "player")
    @Builder.Default
    private List<MatchEvent> matchEvents = new ArrayList<>();

    @OneToMany(mappedBy = "player")
    @Builder.Default
    private List<Sanction> sanctions = new ArrayList<>();

    @Transient
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}