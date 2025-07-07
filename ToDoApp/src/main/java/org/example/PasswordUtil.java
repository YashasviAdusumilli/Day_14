package org.example;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password generation, hashing, and verification.
 */
public class PasswordUtil {

    // 📏 Length of the random password in bytes
    private static final int RANDOM_PASSWORD_BYTES = 12;

    /**
     * Generates a secure random password encoded in Base64.
     */
    public static String generateRandomPassword() {
        byte[] randomBytes = new byte[RANDOM_PASSWORD_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Hashes the plain text password using BCrypt (strength: 12).
     */
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    /**
     * Verifies the password against the stored hash.
     */
    public static boolean verifyPassword(String password, String hash) {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    }
}
