package dev.fkreuzer.shotlog.config;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUserInitializerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DefaultUserInitializer defaultUserInitializer;

    @Test
    void run_shouldCreateAdminUser_whenAdminDoesNotExist() throws Exception {
        // Arrange
        Role adminRole = new Role("ADMIN");
        when(userAccountRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("admin")).thenReturn("encodedAdmin");

        // Act
        defaultUserInitializer.run();

        // Assert
        verify(userAccountRepository).save(argThat(user ->
                user.getUsername()
                        .equals("admin") &&
                        user.getPasswordHash()
                                .equals("encodedAdmin") &&
                        user.getRoles()
                                .contains(adminRole)
        ));
    }

    @Test
    void run_shouldSkipCreation_whenAdminAlreadyExists() throws Exception {
        // Arrange
        UserAccount existingAdmin = new UserAccount("admin", "hash", java.util.Set.of());
        when(userAccountRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        // Act
        defaultUserInitializer.run();

        // Assert
        verify(userAccountRepository, never()).save(any());
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void run_shouldThrowException_whenAdminRoleNotFound() {
        // Arrange
        when(userAccountRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> defaultUserInitializer.run());
    }
}
