package co.edu.uptc.backend_tc.dto.filter;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerFilterDTO {
    private String fullName;
    private String documentNumber;
    private String studentCode;
    private String institutionalEmail;
    private Boolean isActive;
}