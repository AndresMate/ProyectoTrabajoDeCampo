package co.edu.uptc.backend_tc.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordGeneratorService {

    private static final SecureRandom random = new SecureRandom();
    private static final int PASSWORD_LENGTH = 12;

    public String generateSecurePassword() {
        byte[] bytes = new byte[PASSWORD_LENGTH];
        random.nextBytes(bytes);
        String password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return password.substring(0, PASSWORD_LENGTH);
    }
}