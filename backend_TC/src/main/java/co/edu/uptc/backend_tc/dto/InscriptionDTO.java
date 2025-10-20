package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import jakarta.validation.constraints.*;
import lombok.*;

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

    @NotBlank(message = "Delegate name is required")
    @Size(min = 3, max = 100, message = "Delegate name must be between 3 and 100 characters")
    private String delegateName;

    @NotBlank(message = "Delegate email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String delegateEmail;

    @NotBlank(message = "Delegate phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String delegatePhone;
    @NotNull(message = "Delegate player ID is required")
    private Long delegatePlayerId;

    private InscriptionStatus status;
}