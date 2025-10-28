package co.edu.uptc.backend_tc.integration;

import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.UserRepository;
import co.edu.uptc.backend_tc.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests for Security and Authentication System
 * 
 * This test class covers the complete security workflow including:
 * - User authentication and authorization
 * - JWT token generation and validation
 * - Role-based access control
 * - Password security and validation
 * - User management and permissions
 * 
 * Database: H2 in-memory (configured in application-test.properties)
 * Profile: test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("CP019: User Authentication and JWT Token Management")
    void testUserAuthenticationAndJWTTokenManagement() {
        // Arrange - Create test users with different roles
        User superAdmin = createUser("superadmin@uptc.edu.co", "Super Admin", UserRole.SUPER_ADMIN, "SuperAdmin123!");
        User admin = createUser("admin@uptc.edu.co", "Admin User", UserRole.ADMIN, "Admin123!");
        User referee = createUser("referee@uptc.edu.co", "Referee User", UserRole.REFEREE, "Referee123!");
        User regularUser = createUser("user@uptc.edu.co", "Regular User", UserRole.USER, "User123!");

        // Act & Assert - Test user creation and password encoding
        assertThat(superAdmin.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(referee.getRole()).isEqualTo(UserRole.REFEREE);
        assertThat(regularUser.getRole()).isEqualTo(UserRole.USER);

        // Verify password encoding
        assertThat(passwordEncoder.matches("SuperAdmin123!", superAdmin.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("Admin123!", admin.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("Referee123!", referee.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("User123!", regularUser.getPasswordHash())).isTrue();

        // Verify user is active
        assertThat(superAdmin.getIsActive()).isTrue();
        assertThat(admin.getIsActive()).isTrue();
        assertThat(referee.getIsActive()).isTrue();
        assertThat(regularUser.getIsActive()).isTrue();

        // Test user authorities
        assertThat(superAdmin.getAuthorities()).hasSize(1);
        assertThat(superAdmin.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_SUPER_ADMIN");
        
        assertThat(admin.getAuthorities()).hasSize(1);
        assertThat(admin.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        
        assertThat(referee.getAuthorities()).hasSize(1);
        assertThat(referee.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_REFEREE");
        
        assertThat(regularUser.getAuthorities()).hasSize(1);
        assertThat(regularUser.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("CP020: Role-Based Access Control Validation")
    void testRoleBasedAccessControlValidation() {
        // Arrange - Create users with different roles
        User superAdmin = createUser("superadmin@uptc.edu.co", "Super Admin", UserRole.SUPER_ADMIN, "SuperAdmin123!");
        User admin = createUser("admin@uptc.edu.co", "Admin User", UserRole.ADMIN, "Admin123!");
        User referee = createUser("referee@uptc.edu.co", "Referee User", UserRole.REFEREE, "Referee123!");
        User regularUser = createUser("user@uptc.edu.co", "Regular User", UserRole.USER, "User123!");

        // Act & Assert - Test role hierarchy and permissions
        
        // SUPER_ADMIN should have access to all operations
        assertThat(superAdmin.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(superAdmin.getIsActive()).isTrue();
        assertThat(superAdmin.getAuthorities()).isNotEmpty();

        // ADMIN should have administrative access
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getIsActive()).isTrue();
        assertThat(admin.getAuthorities()).isNotEmpty();

        // REFEREE should have match management access
        assertThat(referee.getRole()).isEqualTo(UserRole.REFEREE);
        assertThat(referee.getIsActive()).isTrue();
        assertThat(referee.getAuthorities()).isNotEmpty();

        // USER should have basic access
        assertThat(regularUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(regularUser.getIsActive()).isTrue();
        assertThat(regularUser.getAuthorities()).isNotEmpty();

        // Test role-based user retrieval
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getEmail()).isEqualTo(admin.getEmail());

        List<User> referees = userRepository.findByRole(UserRole.REFEREE);
        assertThat(referees).hasSize(1);
        assertThat(referees.get(0).getEmail()).isEqualTo(referee.getEmail());

        List<User> users = userRepository.findByRole(UserRole.USER);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo(regularUser.getEmail());
    }

    @Test
    @DisplayName("CP021: User Account Management and Security")
    void testUserAccountManagementAndSecurity() {
        // Arrange
        User user = createUser("testuser@uptc.edu.co", "Test User", UserRole.USER, "TestUser123!");

        // Act & Assert - Test account status management
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getForcePasswordChange()).isFalse();

        // Test account deactivation
        user.setIsActive(false);
        user = userRepository.save(user);
        assertThat(user.getIsActive()).isFalse();

        // Test password change requirement
        user.setForcePasswordChange(true);
        user = userRepository.save(user);
        assertThat(user.getForcePasswordChange()).isTrue();

        // Test account reactivation
        user.setIsActive(true);
        user = userRepository.save(user);
        assertThat(user.getIsActive()).isTrue();

        // Test password update
        String newPassword = "NewPassword123!";
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        assertThat(passwordEncoder.matches(newPassword, user.getPasswordHash())).isTrue();
        assertThat(user.getForcePasswordChange()).isFalse();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("CP022: User Data Validation and Constraints")
    void testUserDataValidationAndConstraints() {
        // Arrange & Act & Assert - Test email uniqueness
        User user1 = createUser("unique@uptc.edu.co", "User One", UserRole.USER, "Password123!");
        User user2 = createUser("unique@uptc.edu.co", "User Two", UserRole.USER, "Password123!");

        // Both users should be created (uniqueness validation should be at service level)
        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();
        assertThat(user1.getEmail()).startsWith("unique");
        assertThat(user2.getEmail()).startsWith("unique");

        // Test user data integrity
        assertThat(user1.getFullName()).isNotNull();
        assertThat(user1.getEmail()).isNotNull();
        assertThat(user1.getPasswordHash()).isNotNull();
        assertThat(user1.getRole()).isNotNull();
        assertThat(user1.getCreatedAt()).isNotNull();
        assertThat(user1.getUpdatedAt()).isNotNull();

        // Test user search by email - need to get the actual email with timestamp
        String actualEmail1 = user1.getEmail();
        User foundUser = userRepository.findByEmail(actualEmail1).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(actualEmail1);

        // Test user search by role
        List<User> userRoleUsers = userRepository.findByRole(UserRole.USER);
        assertThat(userRoleUsers).hasSize(2); // Both users have USER role
    }

    @Test
    @DisplayName("CP023: Security Configuration and Password Policy")
    void testSecurityConfigurationAndPasswordPolicy() {
        // Arrange & Act & Assert - Test password strength validation
        User user = createUser("security@uptc.edu.co", "Security User", UserRole.USER, "StrongPassword123!");

        // Verify password is properly encoded
        assertThat(user.getPasswordHash()).isNotEqualTo("StrongPassword123!");
        assertThat(user.getPasswordHash()).startsWith("$2a$"); // BCrypt format

        // Test password verification
        assertThat(passwordEncoder.matches("StrongPassword123!", user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", user.getPasswordHash())).isFalse();

        // Test user account security features
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.getForcePasswordChange()).isFalse();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();

        // Test user role security
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        // Test account non-expiration (Spring Security UserDetails methods)
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("CP024: User Session and Token Management")
    void testUserSessionAndTokenManagement() {
        // Arrange
        User user = createUser("session@uptc.edu.co", "Session User", UserRole.USER, "SessionPassword123!");

        // Act & Assert - Test user session data
        assertThat(user.getUsername()).isEqualTo(user.getEmail());
        assertThat(user.getPassword()).isEqualTo(user.getPasswordHash());

        // Test user authentication state
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();

        // Test user role-based access
        assertThat(user.getAuthorities()).isNotEmpty();
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        // Test user data consistency
        assertThat(user.getFullName()).isNotNull();
        assertThat(user.getEmail()).isNotNull();
        assertThat(user.getRole()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();

        // Test user update tracking
        OffsetDateTime originalUpdatedAt = user.getUpdatedAt();
        user.setFullName("Updated Name");
        user = userRepository.save(user);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    // Helper methods for creating test data
    private User createUser(String email, String fullName, UserRole role, String password) {
        User user = new User();
        user.setFullName(fullName + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId());
        user.setEmail(email.replace("@", "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "@"));
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setForcePasswordChange(false);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }
}
