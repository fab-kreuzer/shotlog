package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.security.SecurityUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    void loadUserByUsername_shouldReturnSecurityUser_whenUserExists() {
        // Arrange
        Role role = new Role("USER");
        UserAccount account = new UserAccount("testUser", "hash", Set.of(role));
        when(userAccountRepository.findByUsername("testUser")).thenReturn(Optional.of(account));

        // Act
        UserDetails result = userDetailService.loadUserByUsername("testUser");

        // Assert
        assertInstanceOf(SecurityUser.class, result);
        assertEquals("testUser", result.getUsername());
        assertEquals("hash", result.getPassword());
        verify(userAccountRepository).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userAccountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("unknown")
        );
        assertEquals("unknown", exception.getMessage());
        verify(userAccountRepository).findByUsername("unknown");
    }
}
