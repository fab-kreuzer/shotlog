package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserAvatar;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.repository.UserAvatarRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private UserAvatarRepository userAvatarRepository;

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
        when(userAvatarRepository.existsById(1L)).thenReturn(true);

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
        assertEquals(true, body.get("hasAvatar"));
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

    // --- updateCurrentUser ---

    @Test
    void updateCurrentUser_shouldReturn401_whenNotAuthenticated() {
        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(Map.of("displayName", "New Name"));

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    @Test
    void updateCurrentUser_shouldUpdateDisplayName() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Old Name", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);

        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(Map.of("displayName", "  New Name  "));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("New Name", account.getDisplayName());
        verify(userAccountRepository).save(account);
    }

    @Test
    void updateCurrentUser_shouldReturnBadRequest_whenDisplayNameBlank() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Old Name", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);

        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(Map.of("displayName", "   "));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        assertEquals("Old Name", account.getDisplayName());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void updateCurrentUser_shouldClearHomeClub_whenHomeClubIdIsNull() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Old Name", new HashSet<>());
        account.setId(1L);
        ShootingPlace club = new ShootingPlace();
        club.setId(5L);
        account.setHomeClub(club);
        setAuthentication(account);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("homeClubId", null);

        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertNull(account.getHomeClub());
        verify(userAccountRepository).save(account);
    }

    @Test
    void updateCurrentUser_shouldSetHomeClub_whenHomeClubIdIsValid() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Old Name", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        ShootingPlace club = new ShootingPlace();
        club.setId(5L);
        club.setClub("Test Club");
        when(shootingPlaceService.findById(5L)).thenReturn(club);

        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(Map.of("homeClubId", 5L));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(club, account.getHomeClub());
        verify(userAccountRepository).save(account);
    }

    @Test
    void updateCurrentUser_shouldReturnBadRequest_whenHomeClubNotFound() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Old Name", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(shootingPlaceService.findById(99L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = controller.updateCurrentUser(Map.of("homeClubId", 99L));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    // --- changePassword ---

    @Test
    void changePassword_shouldReturn401_whenNotAuthenticated() {
        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("currentPassword", "old", "newPassword", "newPassword123"));

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    @Test
    void changePassword_shouldUpdatePasswordHash_whenCurrentPasswordCorrectAndNewPasswordValid() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "oldHash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(passwordEncoder.matches("oldPassword", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");

        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("currentPassword", "oldPassword", "newPassword", "newPassword123"));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("newHash", account.getPasswordHash());
        verify(userAccountRepository).save(account);
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenCurrentPasswordIncorrect() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "oldHash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(passwordEncoder.matches("wrongPassword", "oldHash")).thenReturn(false);

        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("currentPassword", "wrongPassword", "newPassword", "newPassword123"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        assertEquals("oldHash", account.getPasswordHash());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenCurrentPasswordMissing() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "oldHash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);

        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("newPassword", "newPassword123"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenNewPasswordTooShort() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "oldHash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(passwordEncoder.matches("oldPassword", "oldHash")).thenReturn(true);

        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("currentPassword", "oldPassword", "newPassword", "short"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        assertEquals("oldHash", account.getPasswordHash());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenNewPasswordMissing() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "oldHash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(passwordEncoder.matches("oldPassword", "oldHash")).thenReturn(true);

        // Act
        ResponseEntity<?> response = controller.changePassword(Map.of("currentPassword", "oldPassword"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAccountRepository, never()).save(any());
    }

    // --- uploadAvatar ---

    @Test
    void uploadAvatar_shouldReturn401_whenNotAuthenticated() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        // Act
        ResponseEntity<?> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_whenFileEmpty() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);

        // Act
        ResponseEntity<?> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_whenContentTypeNotImage() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        MultipartFile file = new MockMultipartFile("file", "avatar.txt", "text/plain", new byte[]{1, 2, 3});

        // Act
        ResponseEntity<?> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_whenFileTooLarge() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        byte[] tooLarge = new byte[(2 * 1024 * 1024) + 1];
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", tooLarge);

        // Act
        ResponseEntity<?> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadAvatar_shouldSaveAvatar_whenValid() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        byte[] data = new byte[]{1, 2, 3, 4};
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", data);

        // Act
        ResponseEntity<?> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAvatarRepository).save(argThat(avatar ->
                avatar.getUserId()
                        .equals(1L) &&
                        avatar.getContentType()
                                .equals("image/png") &&
                        java.util.Arrays.equals(avatar.getData(), data)
        ));
    }

    // --- getAvatar ---

    @Test
    void getAvatar_shouldReturn401_whenNotAuthenticated() {
        // Act
        ResponseEntity<byte[]> response = controller.getAvatar();

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    @Test
    void getAvatar_shouldReturn404_whenNoAvatarStored() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(userAvatarRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<byte[]> response = controller.getAvatar();

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
    }

    @Test
    void getAvatar_shouldReturnImageData_whenAvatarExists() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        byte[] data = new byte[]{5, 6, 7};
        UserAvatar avatar = new UserAvatar(1L, data, "image/jpeg");
        when(userAvatarRepository.findById(1L)).thenReturn(Optional.of(avatar));

        // Act
        ResponseEntity<byte[]> response = controller.getAvatar();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertTrue(java.util.Arrays.equals(data, response.getBody()));
        assertEquals("image/jpeg", response.getHeaders()
                .getContentType()
                .toString());
    }

    // --- deleteAvatar ---

    @Test
    void deleteAvatar_shouldReturn401_whenNotAuthenticated() {
        // Act
        ResponseEntity<?> response = controller.deleteAvatar();

        // Assert
        assertEquals(401, response.getStatusCode()
                .value());
    }

    @Test
    void deleteAvatar_shouldDeleteAvatar_whenExists() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(userAvatarRepository.existsById(1L)).thenReturn(true);

        // Act
        ResponseEntity<?> response = controller.deleteAvatar();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAvatarRepository).deleteById(1L);
    }

    @Test
    void deleteAvatar_shouldNotCallDelete_whenAvatarDoesNotExist() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", "Test Display", new HashSet<>());
        account.setId(1L);
        setAuthentication(account);
        when(userAvatarRepository.existsById(1L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = controller.deleteAvatar();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(userAvatarRepository, never()).deleteById(any());
    }
}