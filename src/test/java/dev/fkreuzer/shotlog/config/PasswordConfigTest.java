package dev.fkreuzer.shotlog.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordConfigTest {

    private final PasswordConfig config = new PasswordConfig();

    @Test
    void passwordEncoder_shouldReturnBCryptEncoder() {
        // Act
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void passwordEncoder_shouldProduceMatchableHash() {
        // Arrange
        PasswordEncoder encoder = config.passwordEncoder();

        // Act
        String hash = encoder.encode("secret");

        // Assert
        assertNotEquals("secret", hash);
        assertTrue(encoder.matches("secret", hash));
    }
}
