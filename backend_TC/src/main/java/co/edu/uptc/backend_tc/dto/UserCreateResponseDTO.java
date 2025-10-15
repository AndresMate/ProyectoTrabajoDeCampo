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
public class UserCreateResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String temporaryPassword; // Solo se muestra una vez durante la creación
    private boolean forcePasswordChange;
    private String message;

    @Builder.Default
    private boolean success = true;
}