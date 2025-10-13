package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDTO {
    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(min = 3, max = 150, message = "Team name must be between 3 and 150 characters")
    private String name;

    private Boolean isActive;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long originInscriptionId;

    @NotNull(message = "Club ID is required")
    private Long clubId;
}
