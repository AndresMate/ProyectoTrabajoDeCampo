package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInscriptionDTO {

    @NotBlank(message = "Document number is required")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Document must be 6-15 digits")
    private String documentNumber;

    @NotBlank(message = "Student code is required")
    @Size(max = 50)
    private String studentCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 200)
    private String fullName;

    @NotBlank(message = "Institutional email is required")
    @Email(message = "Invalid email format")
    private String institutionalEmail;

    // ✅ URL de la foto (será generada por el backend al subir la imagen)
    private String idCardPhotoUrl;
}