package co.edu.uptc.backend_tc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_documents", indexes = {
        @Index(name = "idx_document_player", columnList = "player_id"),
        @Index(name = "idx_document_type", columnList = "doc_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"player"})
@EqualsAndHashCode(exclude = {"player"})
public class PlayerDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Document type is required")
    @Size(max = 50, message = "Document type cannot exceed 50 characters")
    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @NotBlank(message = "File URI is required")
    @Size(max = 500, message = "File URI cannot exceed 500 characters")
    @Column(name = "file_uri", nullable = false, length = 500)
    private String fileUri;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = OffsetDateTime.now();
    }
}