package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void authorityNames_ReturnsEmptySet_WhenNoRolesAssigned() {
        // Arrange
        UserAccount userAccount = new UserAccount("testUser", "passwordHash", Set.of());

        // Act
        Set<String> authorityNames = userAccount.authorityNames();

        // Assert
        assertTrue(authorityNames.isEmpty(), "Authority names should be empty when no roles are assigned.");
    }

    @Test
    void authorityNames_ReturnsSingleAuthority_WhenOneRoleAssigned() {
        // Arrange
        Role role = new Role("USER");
        UserAccount userAccount = new UserAccount("testUser", "passwordHash", Set.of(role));

        // Act
        Set<String> authorityNames = userAccount.authorityNames();

        // Assert
        assertEquals(1, authorityNames.size(), "Authority names should contain one authority.");
        assertTrue(authorityNames.contains("ROLE_USER"), "Authority names should contain 'ROLE_USER'.");
    }

    @Test
    void authorityNames_ReturnsMultipleAuthorities_WhenMultipleRolesAssigned() {
        // Arrange
        Role roleUser = new Role("USER");
        Role roleAdmin = new Role("ADMIN");
        UserAccount userAccount = new UserAccount("testUser", "passwordHash", Set.of(roleUser, roleAdmin));

        // Act
        Set<String> authorityNames = userAccount.authorityNames();

        // Assert
        assertEquals(2, authorityNames.size(), "Authority names should contain two authorities.");
        assertTrue(authorityNames.contains("ROLE_USER"), "Authority names should contain 'ROLE_USER'.");
        assertTrue(authorityNames.contains("ROLE_ADMIN"), "Authority names should contain 'ROLE_ADMIN'.");
    }

    @Test
    void authorityNames_ReturnsUnmodifiableSet() {
        // Arrange
        Role role = new Role("USER");
        UserAccount userAccount = new UserAccount("testUser", "passwordHash", Set.of(role));

        // Act & Assert
        Set<String> authorityNames = userAccount.authorityNames();
        assertThrows(UnsupportedOperationException.class,
                () -> authorityNames.add("ROLE_ADMIN"),
                "Authority names set should be unmodifiable.");
    }

}