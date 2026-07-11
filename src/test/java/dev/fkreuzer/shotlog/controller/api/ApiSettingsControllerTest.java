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
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSettingsControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ApiSettingsController controller;

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
