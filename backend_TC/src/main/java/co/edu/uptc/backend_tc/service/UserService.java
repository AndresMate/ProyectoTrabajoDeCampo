package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.*;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.exception.UnauthorizedException;
import co.edu.uptc.backend_tc.mapper.UserMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    // === SOLO SUPER_ADMIN PUEDE ACCEDER ===

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PageResponseDTO<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return mapperUtils.mapPage(page, userMapper::toDTO);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<UserDTO> getActiveUsers() {
        return mapperUtils.mapList(
                userRepository.findByIsActiveTrue(),
                userMapper::toDTO
        );
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<UserDTO> getUsersByRole(UserRole role) {
        return mapperUtils.mapList(
                userRepository.findByRole(role),
                userMapper::toDTO
        );
    }

    // === ACCESO MIXTO (SUPER_ADMIN o el propio usuario) ===

    @PreAuthorize("hasRole('SUPER_ADMIN') or authentication.principal.id == #id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or authentication.principal.email == #email")
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    // === SOLO SUPER_ADMIN PUEDE CREAR USUARIOS ===

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UserCreateResponseDTO createUser(UserCreateDTO dto) {
        // Validar email único
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new ConflictException(
                    "User with this email already exists",
                    "email",
                    dto.getEmail()
            );
        }

        // Validar formato de email
        if (!isValidEmail(dto.getEmail())) {
            throw new BadRequestException("Invalid email format");
        }

        // Generar contraseña temporal segura
        String password = dto.getPassword();

        // Crear usuario con contraseña temporal
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase())
                .role(dto.getRole())
                .passwordHash(passwordEncoder.encode(password))
                .isActive(true)
                .forcePasswordChange(true) // Obligar a cambiar contraseña en primer login
                .build();

        user = userRepository.save(user);

        // Retornar DTO con información de la contraseña temporal
        return UserCreateResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .temporaryPassword(password)
                .forcePasswordChange(true)
                .build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or (authentication.principal.id == #id and #dto.role == null)")
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // SOLO SUPER_ADMIN puede cambiar roles
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

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

        // Validar formato de email si se proporciona
        if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) {
            throw new BadRequestException("Invalid email format");
        }

        // Actualizar campos
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().toLowerCase());
        }
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or authentication.principal.id == #id")
    @Transactional
    public void changePassword(Long id, ChangePasswordDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        // Si no es SUPER_ADMIN, validar contraseña actual
        if (!isSuperAdmin) {
            if (dto.getCurrentPassword() == null) {
                throw new BadRequestException("Current password is required");
            }
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Current password is incorrect");
            }
        }

        // Validar que nueva contraseña sea diferente
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Validar fortaleza de nueva contraseña
        if (dto.getNewPassword().length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters long");
        }

        // Encriptar y guardar nueva contraseña
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setForcePasswordChange(false); // Ya no necesita cambiar contraseña

        userRepository.save(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // No permitir desactivarse a sí mismo
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }
        Long currentUserId = ((User) principal).getId();

        if (user.getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot deactivate your own account");
        }

        user.setIsActive(false);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // No permitir eliminarse a sí mismo
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }
        Long currentUserId = ((User) principal).getId();

        if (id.equals(currentUserId)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.getIsActive()) {
            throw new BadRequestException("Cannot reset password for inactive user");
        }

        String temporaryPassword = passwordGeneratorService.generateSecurePassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setForcePasswordChange(true);
        userRepository.save(user);

        return temporaryPassword;
    }

    // === MÉTODOS PRIVADOS DE APOYO ===

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}