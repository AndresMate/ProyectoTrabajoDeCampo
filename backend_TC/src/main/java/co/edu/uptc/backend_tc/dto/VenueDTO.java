package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    private Long id;

    @NotBlank(message = "Venue name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
}