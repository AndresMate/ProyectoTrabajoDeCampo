package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamAvailabilityDTO {

    private Long id;

    @NotBlank(message = "dayOfWeek is required (MONDAY..FRIDAY)")
    private String dayOfWeek; // e.g. "MONDAY"

    @NotBlank(message = "startTime is required (HH:mm)")
    private String startTime; // e.g. "11:00"

    @NotBlank(message = "endTime is required (HH:mm)")
    private String endTime;   // e.g. "12:00"
}
