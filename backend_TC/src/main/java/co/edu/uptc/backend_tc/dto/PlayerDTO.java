package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 200, message = "Full name must be between 3 and 200 characters")
    private String fullName;

    @Size(max = 50, message = "Student code cannot exceed 50 characters")
    private String studentCode;

    @NotBlank(message = "Document number is required")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Document number must be between 6 and 15 digits")
    private String documentNumber;

    @Email(message = "Invalid email format")
    private String institutionalEmail;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    private Boolean isActive;
}
