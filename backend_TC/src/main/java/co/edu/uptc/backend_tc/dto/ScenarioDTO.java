package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioDTO {
    private Long id;

    @NotBlank(message = "Scenario name is required")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    private String name;

    @Positive(message = "Capacity must be a positive number")
    private Integer capacity;

    private Boolean supportsNightGames;

    @NotNull(message = "Venue ID is required")
    private Long venueId;
}