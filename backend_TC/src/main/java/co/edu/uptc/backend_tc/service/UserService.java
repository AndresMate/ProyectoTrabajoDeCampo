package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.UserDTO;
import co.edu.uptc.backend_tc.dto.UserCreateDTO;
import co.edu.uptc.backend_tc.dto.UserUpdateDTO;
import co.edu.uptc.backend_tc.dto.ChangePasswordDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.UserMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MapperUtils mapperUtils;
    // private final PasswordEncoder passwordEncoder; // Descomentar cuando implementes seguridad

    public PageResponseDTO<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return mapperUtils.mapPage(page, userMapper::toDTO);
    }

    public List<UserDTO> getActiveUsers() {
        return mapperUtils.mapList(
                userRepository.findByIsActiveTrue(),
                userMapper::toDTO
        );
    }

    public List<UserDTO> getUsersByRole(UserRole role) {
        return mapperUtils.mapList(
                userRepository.findByRole(role),
                userMapper::toDTO
        );
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO dto) {
        // Validar email único
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ConflictException(
                    "User with this email already exists",
                    "email",
                    dto.getEmail()
            );
        }

        // Validar formato de email
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid email format");
        }

        // Crear usuario
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase())
                .role(dto.getRole())
                .passwordHash(dto.getPassword()) // TODO: Encriptar con passwordEncoder.encode(dto.getPassword())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Validar email único si cambió
        if (dto.getEmail() != null &&
                !user.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ConflictException(
                    "User with this email already exists",
                    "email",
                    dto.getEmail()
            );
        }

        // Actualizar campos
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().toLowerCase());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // TODO: Validar contraseña actual
        // if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
        //     throw new BadRequestException("Current password is incorrect");
        // }

        // TODO: Encriptar nueva contraseña
        // user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordHash(dto.getNewPassword());

        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        // Soft delete preferiblemente
        deactivateUser(id);
    }
}