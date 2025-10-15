package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private String email;
    private String fullName;
    private UserRole role;
    private Long userId;
    private boolean forcePasswordChange;

    @Builder.Default
    private boolean success = true;

    private String message;
}