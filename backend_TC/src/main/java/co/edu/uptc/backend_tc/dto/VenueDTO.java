package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    private Long id;
    private String name;
    private String address;
}
