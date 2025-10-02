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
}
