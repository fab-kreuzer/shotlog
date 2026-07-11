package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.security.SecurityUser;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiAuthControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ShootingPlaceService shootingPlaceService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ApiAuthController controller;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(UserAccount account) {
        SecurityUser securityUser = new SecurityUser(account);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(auth);
    }

    // --- getCurrentUserInfo ---

    @Test
    void getCurrentUserInfo_shouldReturnUserInfo_whenAuthenticated() {
        // Arrange
        Role role = new Role("USER");
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", Set.of(role));
        account.setId(1L);
        setAuthentication(account);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.getCurrentUserInfo();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(1L, body.get("id"));
        assertEquals("testUser", body.get("username"));
        assertEquals("Test Display", body.get("displayName"));
    }

    @Test
    void getCurrentUserInfo_shouldReturn401_whenNotAuthenticated() {
        // Act
        ResponseEntity<Map<String, Object>> response = controller.getCurrentUserInfo();

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    // --- register ---

    @Test
    void register_shouldCreateUser_withValidInput() {
        // Arrange
        Role userRole = new Role("USER");
        when(userAccountRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        Map<String, String> body = Map.of(
                "username", "newUser",
                "password", "password123",
                "displayName", "New User"
        );

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAccountRepository).save(argThat(user ->
                user.getUsername()
                        .equals("newUser") &&
                        user.getPasswordHash()
                                .equals("encodedPassword") &&
                        user.getDisplayName()
                                .equals("New User")
        ));
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameIsBlank() {
        // Arrange
        Map<String, String> body = Map.of("username", "", "password", "password123");

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameIsNull() {
        // Arrange
        Map<String, String> body = Map.of("password", "password123");

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordTooShort() {
        // Arrange
        Map<String, String> body = Map.of("username", "user", "password", "12345");

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsNull() {
        // Arrange
        Map<String, String> body = Map.of("username", "user");

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
    }

    @Test
    void register_shouldReturnBadRequest_whenUsernameAlreadyExists() {
        // Arrange
        Map<String, String> body = Map.of(
                "username", "existingUser",
                "password", "password123"
        );

        // Act
        ResponseEntity<?> response = controller.register(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }
}