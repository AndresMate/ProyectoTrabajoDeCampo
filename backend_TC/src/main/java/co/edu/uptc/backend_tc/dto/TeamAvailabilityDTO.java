package co.edu.uptc.backend_tc.dto;

import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAvailabilityDTO {
    private Long id;
    private Long teamId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
}
