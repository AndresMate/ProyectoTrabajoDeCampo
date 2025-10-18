package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.UserRole;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isActive;
    private OffsetDateTime createdAt;
}