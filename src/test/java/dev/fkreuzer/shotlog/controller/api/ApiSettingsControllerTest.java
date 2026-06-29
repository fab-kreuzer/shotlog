package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSettingsControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApiSettingsController controller;

    // ---- User Management ----

    @Test
    void getUsers_shouldReturnMappedUsers() {
        // Arrange
        Role role = new Role("USER");
        role.setId(1L);
        UserAccount user = new UserAccount("user1", "hash", "Display", Set.of(role));
        user.setId(1L);
        when(userAccountRepository.findAll()).thenReturn(List.of(user));

        // Act
        ResponseEntity<List<Map<String, Object>>> response = controller.getUsers();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(1, response.getBody()
                .size());
        Map<String, Object> mapped = response.getBody()
                .get(0);
        assertEquals(1L, mapped.get("id"));
        assertEquals("user1", mapped.get("username"));
        assertEquals("Display", mapped.get("displayName"));
    }

    @Test
    void createUser_shouldCreateUser_whenUsernameNotTaken() {
        // Arrange
        when(userAccountRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");

        Map<String, Object> body = new HashMap<>();
        body.put("username", "newUser");
        body.put("password", "pass123");
        body.put("displayName", "New User");
        body.put("roleIds", List.of());

        // Act
        ResponseEntity<?> response = controller.createUser(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void createUser_shouldReturnBadRequest_whenUsernameMissing() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("password", "pass123");
        body.put("displayName", "New User");

        // Act
        ResponseEntity<?> response = controller.createUser(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenDisplayNameMissing() {
        // Arrange
        Map<String, Object> body = new HashMap<>();
        body.put("username", "newUser");
        body.put("password", "pass123");

        // Act
        ResponseEntity<?> response = controller.createUser(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenUsernameExists() {
        // Arrange
        UserAccount existing = new UserAccount("taken", "hash", Set.of());
        when(userAccountRepository.findByUsername("taken")).thenReturn(Optional.of(existing));

        Map<String, Object> body = new HashMap<>();
        body.put("username", "taken");
        body.put("password", "pass123");
        body.put("displayName", "Taken User");

        // Act
        ResponseEntity<?> response = controller.createUser(body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createUser_withRoleIds_shouldResolveRoles() {
        // Arrange
        Role role = new Role("USER");
        role.setId(1L);
        when(userAccountRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        Map<String, Object> body = new HashMap<>();
        body.put("username", "newUser");
        body.put("password", "pass123");
        body.put("displayName", "New User");
        body.put("roleIds", List.of(1));

        // Act
        ResponseEntity<?> response = controller.createUser(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAccountRepository).save(argThat(user ->
                user.getRoles()
                        .contains(role)
        ));
    }

    @Test
    void updateUser_shouldUpdateUsername_whenValid() {
        // Arrange
        UserAccount user = new UserAccount("old", "hash", Set.of());
        user.setId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.findByUsername("newName")).thenReturn(Optional.empty());

        Map<String, Object> body = new HashMap<>();
        body.put("username", "newName");

        // Act
        ResponseEntity<?> response = controller.updateUser(1L, body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("newName", user.getUsername());
        verify(userAccountRepository).save(user);
    }

    @Test
    void updateUser_shouldReturn404_whenUserNotFound() {
        // Arrange
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.updateUser(99L, Map.of());

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
    }

    @Test
    void updateUser_shouldReturnBadRequest_whenUsernameAlreadyTakenByOtherUser() {
        // Arrange
        UserAccount user = new UserAccount("current", "hash", Set.of());
        user.setId(1L);
        UserAccount otherUser = new UserAccount("taken", "hash", Set.of());
        otherUser.setId(2L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.findByUsername("taken")).thenReturn(Optional.of(otherUser));

        Map<String, Object> body = new HashMap<>();
        body.put("username", "taken");

        // Act
        ResponseEntity<?> response = controller.updateUser(1L, body);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
    }

    @Test
    void updateUser_shouldUpdatePassword_whenProvided() {
        // Arrange
        UserAccount user = new UserAccount("user", "oldHash", Set.of());
        user.setId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");

        Map<String, Object> body = new HashMap<>();
        body.put("password", "newPass");

        // Act
        ResponseEntity<?> response = controller.updateUser(1L, body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("newHash", user.getPasswordHash());
    }

    @Test
    void updateUser_shouldUpdateRoles_whenRoleIdsProvided() {
        // Arrange
        UserAccount user = new UserAccount("user", "hash", Set.of());
        user.setId(1L);
        Role adminRole = new Role("ADMIN");
        adminRole.setId(2L);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(adminRole));

        Map<String, Object> body = new HashMap<>();
        body.put("roleIds", List.of(2));

        // Act
        ResponseEntity<?> response = controller.updateUser(1L, body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertTrue(user.getRoles()
                .contains(adminRole));
    }

    @Test
    void deleteUser_shouldDelete_whenUserExists() {
        // Arrange
        UserAccount user = new UserAccount("user1", "hash", "Display", Set.of());
        user.setId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<?> response = controller.deleteUser(1L);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAccountRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() {
        // Arrange
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.deleteUser(99L);

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).deleteById(any());
    }

    // ---- Role Management ----

    @Test
    void getRoles_shouldReturnAllRoles() {
        // Arrange
        Role role = new Role("ADMIN");
        role.setId(1L);
        when(roleRepository.findAll()).thenReturn(List.of(role));

        // Act
        ResponseEntity<List<Map<String, Object>>> response = controller.getRoles();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(1, response.getBody()
                .size());
        assertEquals("ADMIN", response.getBody()
                .get(0)
                .get("name"));
    }

    @Test
    void createRole_shouldCreateRole_whenNameNotTaken() {
        // Arrange
        when(roleRepository.findByName("MODERATOR")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.createRole(Map.of("name", "MODERATOR"));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(roleRepository).save(argThat(role -> role.getName()
                .equals("MODERATOR")));
    }

    @Test
    void createRole_shouldReturnBadRequest_whenRoleExists() {
        // Arrange
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));

        // Act
        ResponseEntity<?> response = controller.createRole(Map.of("name", "ADMIN"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void updateRole_shouldUpdateName_whenValid() {
        // Arrange
        Role role = new Role("OLD");
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("NEW")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.updateRole(1L, Map.of("name", "NEW"));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("NEW", role.getName());
        verify(roleRepository).save(role);
    }

    @Test
    void updateRole_shouldReturn404_whenRoleNotFound() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.updateRole(99L, Map.of("name", "NEW"));

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
    }

    @Test
    void updateRole_shouldReturnBadRequest_whenNameAlreadyTakenByOtherRole() {
        // Arrange
        Role role = new Role("CURRENT");
        role.setId(1L);
        Role otherRole = new Role("TAKEN");
        otherRole.setId(2L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("TAKEN")).thenReturn(Optional.of(otherRole));

        // Act
        ResponseEntity<?> response = controller.updateRole(1L, Map.of("name", "TAKEN"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
    }

    @Test
    void deleteRole_shouldDelete_whenRoleExistsAndNotAssigned() {
        // Arrange
        Role role = new Role("USER");
        role.setId(1L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userAccountRepository.findAll()).thenReturn(List.of());

        // Act
        ResponseEntity<?> response = controller.deleteRole(1L);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(roleRepository).deleteById(1L);
    }

    @Test
    void deleteRole_shouldReturn404_whenRoleNotFound() {
        // Arrange
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = controller.deleteRole(99L);

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
    }

    @Test
    void deleteRole_shouldReturnBadRequest_whenRoleIsAssignedToUsers() {
        // Arrange
        Role role = new Role("USER");
        role.setId(1L);
        UserAccount user = new UserAccount("user1", "hash", "Display", Set.of(role));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userAccountRepository.findAll()).thenReturn(List.of(user));

        // Act
        ResponseEntity<?> response = controller.deleteRole(1L);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(roleRepository, never()).deleteById(any());
    }
}
