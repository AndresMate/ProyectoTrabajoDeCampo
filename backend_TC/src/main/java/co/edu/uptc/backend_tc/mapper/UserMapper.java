package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.UserDTO;
import co.edu.uptc.backend_tc.entity.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }

    public static User toEntity(UserDTO dto) {
        return User.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .role(dto.getRole())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                // ⚠️ El passwordHash debería manejarse en otro flujo (ej. registro con seguridad)
                .build();
    }
}
