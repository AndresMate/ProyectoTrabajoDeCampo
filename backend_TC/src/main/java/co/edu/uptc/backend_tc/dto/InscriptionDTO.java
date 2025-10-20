package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscriptionDTO {
    private Long id;

    @NotBlank(message = "El nombre del equipo es requerido")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    private String teamName;

    @NotBlank(message = "Delegate phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String delegatePhone;

    private InscriptionStatus status;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Delegate player ID is required")
    private Long delegatePlayerId;

    private Long clubId; // opcional según modalidad
}