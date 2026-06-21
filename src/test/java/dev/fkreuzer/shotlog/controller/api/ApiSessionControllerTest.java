package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.security.SecurityUser;
import dev.fkreuzer.shotlog.service.SessionService;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSessionControllerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private ShootingPlaceService shootingPlaceService;

    @InjectMocks
    private ApiSessionController controller;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserAccount("testUser", "hash", Set.of(new Role("USER")));
        SecurityUser securityUser = new SecurityUser(testUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- getAllSessions ---

    @Test
    void getAllSessions_shouldReturnAllUserSessions() {
        // Arrange
        List<Session> sessions = List.of(new Session(), new Session());
        when(sessionService.findAllByUser(testUser)).thenReturn(sessions);

        // Act
        ResponseEntity<List<Session>> response = controller.getAllSessions();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(2, response.getBody()
                .size());
    }

    // --- getSessionsByType ---

    @Test
    void getSessionsByType_shouldReturnSessionsForGivenType() {
        // Arrange
        List<Session> sessions = List.of(new Session());
        when(sessionService.findAllByUserAndType(eq(testUser), any())).thenReturn(sessions);

        // Act
        ResponseEntity<List<Session>> response = controller.getSessionsByType("TRAINING");

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(1, response.getBody()
                .size());
    }

    @Test
    void getSessionsByType_shouldBeCaseInsensitive() {
        // Arrange
        when(sessionService.findAllByUserAndType(eq(testUser), any())).thenReturn(List.of());

        // Act
        ResponseEntity<List<Session>> response = controller.getSessionsByType("training");

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
    }

    // --- getSession ---

    @Test
    void getSession_shouldReturnSession_whenFound() {
        // Arrange
        Session session = new Session();
        when(sessionService.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(session));

        // Act
        ResponseEntity<Session> response = controller.getSession(1L);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertSame(session, response.getBody());
    }

    @Test
    void getSession_shouldReturn404_whenNotFound() {
        // Arrange
        when(sessionService.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Session> response = controller.getSession(99L);

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
    }

    // --- createSession ---

    @Test
    void createSession_shouldCreateAndReturnSession() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setId(1L);
        when(shootingPlaceService.findById(1L)).thenReturn(place);
        when(sessionService.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> body = new HashMap<>();
        body.put("sessionDate", "2024-01-15");
        body.put("sessionTime", "14:30");
        body.put("sessionType", "TRAINING");
        body.put("decimalScoring", true);
        body.put("home", true);
        body.put("enemyId", 1);
        body.put("series", List.of());

        // Act
        ResponseEntity<Session> response = controller.createSession(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertNotNull(response.getBody());
        assertSame(testUser, response.getBody()
                .getUser());
        verify(sessionService).save(any(Session.class));
    }

    @Test
    void createSession_withSeriesAndShots_shouldMapCorrectly() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        when(shootingPlaceService.findById(1L)).thenReturn(place);
        when(sessionService.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> shotData = new HashMap<>();
        shotData.put("shotNumber", 1);
        shotData.put("value", "9.5");

        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("seriesNumber", 1);
        seriesData.put("testShot", false);
        seriesData.put("shots", List.of(shotData));

        Map<String, Object> body = new HashMap<>();
        body.put("sessionDate", "2024-01-15");
        body.put("sessionTime", "14:30");
        body.put("sessionType", "TRAINING");
        body.put("decimalScoring", false);
        body.put("home", false);
        body.put("enemyId", 1);
        body.put("series", List.of(seriesData));

        // Act
        ResponseEntity<Session> response = controller.createSession(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        Session session = response.getBody();
        assertNotNull(session);
        assertEquals(1, session.getSeries()
                .size());
        assertEquals(1, session.getSeries()
                .get(0)
                .getShots()
                .size());
    }

    // --- updateSession ---

    @Test
    void updateSession_shouldSetIdAndSave() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        when(shootingPlaceService.findById(1L)).thenReturn(place);
        when(sessionService.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> body = new HashMap<>();
        body.put("sessionDate", "2024-01-15");
        body.put("sessionTime", "14:30");
        body.put("sessionType", "COMPETITION");
        body.put("decimalScoring", false);
        body.put("home", false);
        body.put("enemyId", 1);
        body.put("series", List.of());

        // Act
        ResponseEntity<Session> response = controller.updateSession(5L, body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(5L, response.getBody()
                .getId());
        assertSame(testUser, response.getBody()
                .getUser());
    }

    // --- deleteSession ---

    @Test
    void deleteSession_shouldReturnNoContent() {
        // Act
        ResponseEntity<Void> response = controller.deleteSession(1L);

        // Assert
        assertEquals(204, response.getStatusCode()
                .value());
        verify(sessionService).deleteByIdAndUser(1L, testUser);
    }

    // --- mapToSession edge cases ---

    @Test
    void createSession_withNullSeries_shouldSetEmptyList() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        when(shootingPlaceService.findById(1L)).thenReturn(place);
        when(sessionService.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> body = new HashMap<>();
        body.put("sessionDate", "2024-01-15");
        body.put("sessionTime", "14:30");
        body.put("sessionType", "TRAINING");
        body.put("decimalScoring", false);
        body.put("home", false);
        body.put("enemyId", 1);
        // No "series" key

        // Act
        ResponseEntity<Session> response = controller.createSession(body);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertTrue(response.getBody()
                .getSeries()
                .isEmpty());
    }

    @Test
    void createSession_withNullShotValue_shouldLeaveValueNull() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        when(shootingPlaceService.findById(1L)).thenReturn(place);
        when(sessionService.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> shotData = new HashMap<>();
        shotData.put("shotNumber", 1);
        shotData.put("value", null);

        Map<String, Object> seriesData = new HashMap<>();
        seriesData.put("seriesNumber", 1);
        seriesData.put("testShot", false);
        seriesData.put("shots", List.of(shotData));

        Map<String, Object> body = new HashMap<>();
        body.put("sessionDate", "2024-01-15");
        body.put("sessionTime", "14:30");
        body.put("sessionType", "TRAINING");
        body.put("decimalScoring", false);
        body.put("home", false);
        body.put("enemyId", 1);
        body.put("series", List.of(seriesData));

        // Act
        ResponseEntity<Session> response = controller.createSession(body);

        // Assert
        assertNull(response.getBody()
                .getSeries()
                .get(0)
                .getShots()
                .get(0)
                .getValue());
    }
}
