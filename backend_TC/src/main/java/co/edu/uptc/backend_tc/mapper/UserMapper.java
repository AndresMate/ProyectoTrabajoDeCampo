package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.UserDTO;
import co.edu.uptc.backend_tc.dto.response.UserSummaryDTO;
import co.edu.uptc.backend_tc.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User entity) {
        if (entity == null) return null;

        return UserDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public UserSummaryDTO toSummaryDTO(User referee) {
        if (referee == null) return null;

        return UserSummaryDTO.builder()
                .id(referee.getId())
                .fullName(referee.getFullName())
                .email(referee.getEmail())
                .role(referee.getRole())
                .build();
    }
}