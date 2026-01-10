package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTest {

    @ParameterizedTest
    @CsvSource({
            "ADMIN, ROLE_ADMIN",
            "'', ROLE_",
            "null, ROLE_null"
    })
    void authority_shouldReturnExpectedRole(String inputName, String expectedAuthority) {
        // Arrange
        Role role = new Role();
        role.setName(inputName);

        // Act
        String authority = role.authority();

        // Assert
        assertEquals(expectedAuthority, authority);
    }
}