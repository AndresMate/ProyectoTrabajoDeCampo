package co.edu.uptc.backend_tc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetResponseDTO {
    private String temporaryPassword;
}