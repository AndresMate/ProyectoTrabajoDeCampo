package co.edu.uptc.backend_tc.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {
    private Long id;
    private String fullName;
    private String studentCode;
    private String documentNumber;
    private String institutionalEmail;
    private LocalDate birthDate;
    private Boolean isActive;
}
