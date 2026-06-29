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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevSecurityConfigTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private DevSecurityConfig config;

    @Test
    void seedAdmin_shouldCreateAdmin_whenAdminDoesNotExist() {
        // Arrange
        Role adminRole = new Role("ADMIN");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("admin")).thenReturn("encodedAdmin");

        // Act
        config.seedAdmin();

        // Assert
        verify(userRepo).save(argThat(user ->
                user.getUsername().equals("admin")
                        && user.getPasswordHash().equals("encodedAdmin")
                        && user.getRoles().contains(adminRole)));
    }

    @Test
    void seedAdmin_shouldSkip_whenAdminAlreadyExists() {
        // Arrange
        UserAccount existing = new UserAccount("admin", "hash", Set.of());
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(existing));

        // Act
        config.seedAdmin();

        // Assert
        verify(userRepo, never()).save(any());
        verify(roleRepo, never()).findByName(any());
    }

    @Test
    void seedAdmin_shouldThrow_whenAdminRoleNotFound() {
        // Arrange
        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> config.seedAdmin());
    }
}
