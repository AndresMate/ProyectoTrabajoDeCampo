package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    @Size(min = 3, max = 200, message = "Full name must be between 3 and 200 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private UserRole role;
    private Boolean isActive;
}