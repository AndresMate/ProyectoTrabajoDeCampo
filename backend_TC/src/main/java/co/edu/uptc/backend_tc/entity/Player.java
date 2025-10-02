package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true)
    private String studentCode;

    @Column(name = "document_number", unique = true, nullable = false)
    private String documentNumber;

    @Column(unique = true)
    private String institutionalEmail;

    private LocalDate birthDate;
    private Boolean isActive = true;
}
