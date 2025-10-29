package co.edu.uptc.backend_tc.fixtures;

import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.model.UserRole;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Data Builder para crear instancias de User con datos de prueba
 */
public class UserTestDataBuilder {
    
    private Long id = 1L;
    private String fullName = "Usuario Test";
    private String email = "test@uptc.edu.co";
    private UserRole role = UserRole.USER;
    private String passwordHash = "$2a$10$test.hash.password";
    private Boolean isActive = true;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    private Boolean forcePasswordChange = false;
    private List<Tournament> createdTournaments = new ArrayList<>();
    private List<Match> refereedMatches = new ArrayList<>();
    
    public UserTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public UserTestDataBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }
    
    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public UserTestDataBuilder withRole(UserRole role) {
        this.role = role;
        return this;
    }
    
    public UserTestDataBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }
    
    public UserTestDataBuilder withIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    public UserTestDataBuilder withCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public UserTestDataBuilder withUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public UserTestDataBuilder withForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
        return this;
    }
    
    public UserTestDataBuilder withCreatedTournaments(List<Tournament> createdTournaments) {
        this.createdTournaments = createdTournaments;
        return this;
    }
    
    public UserTestDataBuilder withRefereedMatches(List<Match> refereedMatches) {
        this.refereedMatches = refereedMatches;
        return this;
    }
    
    public User build() {
        return User.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .role(role)
                .passwordHash(passwordHash)
                .isActive(isActive)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .forcePasswordChange(forcePasswordChange)
                .createdTournaments(createdTournaments)
                .refereedMatches(refereedMatches)
                .build();
    }
    
    // Métodos de conveniencia para casos comunes
    public static UserTestDataBuilder aValidUser() {
        return new UserTestDataBuilder();
    }
    
    public static UserTestDataBuilder anAdminUser() {
        return new UserTestDataBuilder()
                .withRole(UserRole.ADMIN)
                .withEmail("admin@uptc.edu.co");
    }
    
    public static UserTestDataBuilder aSuperAdminUser() {
        return new UserTestDataBuilder()
                .withRole(UserRole.SUPER_ADMIN)
                .withEmail("superadmin@uptc.edu.co");
    }
    
    public static UserTestDataBuilder aRefereeUser() {
        return new UserTestDataBuilder()
                .withRole(UserRole.REFEREE)
                .withEmail("referee@uptc.edu.co");
    }
    
    public static UserTestDataBuilder aPlayerUser() {
        return new UserTestDataBuilder()
                .withRole(UserRole.USER)
                .withEmail("player@uptc.edu.co");
    }
    
    public static UserTestDataBuilder anInactiveUser() {
        return new UserTestDataBuilder()
                .withIsActive(false);
    }
}
