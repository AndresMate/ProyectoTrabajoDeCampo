package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDTO {
    private Long id;

    @NotBlank(message = "Tournament name is required")
    @Size(min = 5, max = 200, message = "Name must be between 5 and 200 characters")
    private String name;

    @Positive(message = "Maximum teams must be a positive number")
    private Integer maxTeams;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Modality is required")
    private Modality modality;

    private TournamentStatus status;

    @NotNull(message = "Sport ID is required")
    private Long sportId;

    @NotNull(message = "Creator ID is required")
    private Long createdById;
}