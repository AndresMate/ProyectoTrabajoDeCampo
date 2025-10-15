package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean isActive;
    private boolean forcePasswordChange;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLogin;
    private Integer createdTournamentsCount;
    private Integer refereedMatchesCount;

    @Builder.Default
    private boolean canEditProfile = true;
    @Builder.Default
    private boolean canChangePassword = true;
}