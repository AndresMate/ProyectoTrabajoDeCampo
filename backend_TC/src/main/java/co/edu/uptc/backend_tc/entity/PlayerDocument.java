package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "player_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String docType;
    private String fileUri;
    private OffsetDateTime uploadedAt = OffsetDateTime.now();

    @ManyToOne @JoinColumn(name = "player_id", nullable = false)
    private Player player;
}
