package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionDTO {

    private Long id;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long clubId;

    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 100, message = "Team name must be between 3 and 100 characters")
    private String teamName;

    // ✅ NUEVA LISTA DE JUGADORES
    @NotEmpty(message = "At least one player is required")
    private List<PlayerInscriptionDTO> players;

    // ✅ ÍNDICE DEL DELEGADO EN LA LISTA
    @NotNull(message = "Delegate index is required")
    @Min(value = 0, message = "Delegate index must be >= 0")
    private Integer delegateIndex;

    // ✅ TELÉFONO DEL DELEGADO (opcional)
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String delegatePhone;

    private InscriptionStatus status;
}