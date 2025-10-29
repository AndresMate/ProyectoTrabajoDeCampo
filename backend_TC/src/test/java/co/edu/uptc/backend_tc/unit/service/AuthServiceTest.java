package co.edu.uptc.backend_tc.unit.service;

import co.edu.uptc.backend_tc.dto.LoginRequestDTO;
import co.edu.uptc.backend_tc.dto.LoginResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.exception.UnauthorizedException;
import co.edu.uptc.backend_tc.fixtures.TournamentFixtures;
import co.edu.uptc.backend_tc.repository.UserRepository;
import co.edu.uptc.backend_tc.service.AuthService;
import co.edu.uptc.backend_tc.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para AuthService
 * 
 * Estas pruebas validan la lógica de autenticación y autorización
 * utilizando mocks para aislar las dependencias externas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User validUser;
    private LoginRequestDTO validLoginRequest;
    private LoginResponseDTO validLoginResponse;
    private Authentication validAuthentication;

    @BeforeEach
    void setUp() {
        // Setup test data
        validUser = TournamentFixtures.adminUser();
        validUser.setForcePasswordChange(false);
        
        validLoginRequest = LoginRequestDTO.builder()
                .email("admin@uptc.edu.co")
                .password("password123")
                .build();
                
        validLoginResponse = LoginResponseDTO.builder()
                .token("jwt-token-123")
                .email("admin@uptc.edu.co")
                .fullName("Admin User")
                .role(validUser.getRole())
                .forcePasswordChange(false)
                .userId(1L)
                .build();
                
        validAuthentication = mock(Authentication.class);
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Should return login response when valid credentials are provided")
    void testLogin_WithValidCredentials_ShouldReturnLoginResponseDTO() {
        // Arrange
        when(validAuthentication.getPrincipal()).thenReturn(validUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(validAuthentication);
        when(jwtService.generateToken(validUser)).thenReturn("jwt-token-123");

        // Act
        LoginResponseDTO result = authService.login(validLoginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-123");
        assertThat(result.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(result.getFullName()).isEqualTo(validUser.getFullName());
        assertThat(result.getRole()).isEqualTo(validUser.getRole());
        assertThat(result.isForcePasswordChange()).isEqualTo(validUser.getForcePasswordChange());
        assertThat(result.getUserId()).isEqualTo(validUser.getId());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(validUser);
        
        // Verify SecurityContext is set
        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(contextAuth).isNotNull();
        assertThat(contextAuth.getPrincipal()).isEqualTo(validUser);
    }

    @Test
    @DisplayName("Should return login response with force password change flag when user requires password change")
    void testLogin_WithUserRequiringPasswordChange_ShouldReturnLoginResponseWithForcePasswordChange() {
        // Arrange
        validUser.setForcePasswordChange(true);
        when(validAuthentication.getPrincipal()).thenReturn(validUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(validAuthentication);
        when(jwtService.generateToken(validUser)).thenReturn("jwt-token-123");

        // Act
        LoginResponseDTO result = authService.login(validLoginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isForcePasswordChange()).isTrue();
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(validUser);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when invalid credentials are provided")
    void testLogin_WithInvalidCredentials_ShouldThrowUnauthorizedException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when authentication fails")
    void testLogin_WithAuthenticationFailure_ShouldThrowUnauthorizedException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate with correct email and password")
    void testLogin_ShouldAuthenticateWithCorrectCredentials() {
        // Arrange
        when(validAuthentication.getPrincipal()).thenReturn(validUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(validAuthentication);
        when(jwtService.generateToken(validUser)).thenReturn("jwt-token-123");

        // Act
        authService.login(validLoginRequest);

        // Assert
        verify(authenticationManager).authenticate(argThat(token -> {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) token;
            return authToken.getPrincipal().equals(validLoginRequest.getEmail()) &&
                   authToken.getCredentials().equals(validLoginRequest.getPassword());
        }));
    }

    // ========== FORCE PASSWORD CHANGE TESTS ==========

    @Test
    @DisplayName("Should change password when valid user ID and new password are provided")
    void testForcePasswordChange_WithValidUserIdAndPassword_ShouldChangePasswordSuccessfully() {
        // Arrange
        String newPassword = "newPassword123";
        String encodedPassword = "encoded-password-hash";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        authService.forcePasswordChange(1L, newPassword);

        // Assert
        assertThat(validUser.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(validUser.getForcePasswordChange()).isFalse();
        
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(validUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void testForcePasswordChange_WithNonExistingUserId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.forcePasswordChange(1L, "newPassword123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id")
                .hasMessageContaining("1");
        
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should encode password before saving")
    void testForcePasswordChange_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String newPassword = "newPassword123";
        String encodedPassword = "encoded-password-hash";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        authService.forcePasswordChange(1L, newPassword);

        // Assert
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(argThat(user -> 
            user.getPasswordHash().equals(encodedPassword) && 
            !user.getForcePasswordChange()
        ));
    }

    @Test
    @DisplayName("Should set forcePasswordChange to false after password change")
    void testForcePasswordChange_ShouldSetForcePasswordChangeToFalse() {
        // Arrange
        validUser.setForcePasswordChange(true);
        String newPassword = "newPassword123";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        authService.forcePasswordChange(1L, newPassword);

        // Assert
        assertThat(validUser.getForcePasswordChange()).isFalse();
        
        verify(userRepository).save(argThat(user -> !user.getForcePasswordChange()));
    }

    // ========== EDGE CASES TESTS ==========

    @Test
    @DisplayName("Should handle null password in force password change")
    void testForcePasswordChange_WithNullPassword_ShouldHandleGracefully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode(null)).thenReturn("encoded-null");

        // Act
        authService.forcePasswordChange(1L, null);

        // Assert
        verify(passwordEncoder).encode(null);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle empty password in force password change")
    void testForcePasswordChange_WithEmptyPassword_ShouldHandleGracefully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode("")).thenReturn("encoded-empty");

        // Act
        authService.forcePasswordChange(1L, "");

        // Assert
        verify(passwordEncoder).encode("");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle different user roles in login")
    void testLogin_WithDifferentUserRoles_ShouldReturnCorrectRole() {
        // Arrange
        User playerUser = TournamentFixtures.playerUser();
        Authentication playerAuth = mock(Authentication.class);
        when(playerAuth.getPrincipal()).thenReturn(playerUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(playerAuth);
        when(jwtService.generateToken(playerUser)).thenReturn("jwt-token-123");

        LoginRequestDTO playerLoginRequest = LoginRequestDTO.builder()
                .email("player@uptc.edu.co")
                .password("password123")
                .build();

        // Act
        LoginResponseDTO result = authService.login(playerLoginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(playerUser.getRole());
        assertThat(result.getEmail()).isEqualTo(playerUser.getEmail());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(playerUser);
    }

    @Test
    @DisplayName("Should handle inactive user in login")
    void testLogin_WithInactiveUser_ShouldStillAuthenticate() {
        // Arrange
        User inactiveUser = TournamentFixtures.inactiveUser();
        Authentication inactiveAuth = mock(Authentication.class);
        when(inactiveAuth.getPrincipal()).thenReturn(inactiveUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(inactiveAuth);
        when(jwtService.generateToken(inactiveUser)).thenReturn("jwt-token-123");

        LoginRequestDTO inactiveLoginRequest = LoginRequestDTO.builder()
                .email("inactive@uptc.edu.co")
                .password("password123")
                .build();

        // Act
        LoginResponseDTO result = authService.login(inactiveLoginRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(inactiveUser.getEmail());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(inactiveUser);
    }
}
