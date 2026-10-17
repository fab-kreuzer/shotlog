package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.*;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.dto.ScanRequest;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.security.SecurityUser;
import dev.fkreuzer.shotlog.service.DisagQrService;
import dev.fkreuzer.shotlog.service.SessionService;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiDisagControllerTest {

    @Mock private DisagQrService disagQrService;
    @Mock private SessionService sessionService;
    @Mock private ShootingPlaceService shootingPlaceService;
    @Mock private SeasonRepository seasonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private ApiDisagController controller;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserAccount("testUser", "hash", Set.of(new Role("USER")));
        SecurityUser securityUser = new SecurityUser(testUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Session scanned() {
        Session s = new Session();
        s.setSessionDate(LocalDate.of(2026, 10, 14));
        s.setExternalId("uuid-1");
        s.setSeries(new ArrayList<>());
        return s;
    }

    @Test
    void scan_createsNewSessionWithChosenTypeAndSeason() {
        // Arrange
        when(disagQrService.decode("u")).thenReturn(null);
        when(disagQrService.toSession(null)).thenReturn(scanned());
        when(sessionService.existsByUserAndExternalId(testUser, "uuid-1")).thenReturn(false);
        when(shootingPlaceService.findById(2L)).thenReturn(new ShootingPlace());
        when(seasonRepository.findById(3L)).thenReturn(Optional.of(new Season("2026")));
        when(sessionService.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<?> response = controller.scan(new ScanRequest("u", SessionType.COMPETITION, 3L, 2L, null, null, null));

        // Assert
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionService).save(captor.capture());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SessionType.COMPETITION, captor.getValue().getSessionType());
        assertEquals(null, captor.getValue().getId());
    }

    @Test
    void scan_overwriteKeepsExistingSessionId() {
        // Arrange
        Session existing = new Session();
        existing.setId(9L);
        when(disagQrService.decode("u")).thenReturn(null);
        when(disagQrService.toSession(null)).thenReturn(scanned());
        when(shootingPlaceService.findById(2L)).thenReturn(new ShootingPlace());
        when(seasonRepository.findById(3L)).thenReturn(Optional.of(new Season("2026")));
        when(sessionService.findByIdAndUser(9L, testUser)).thenReturn(Optional.of(existing));
        when(sessionService.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        controller.scan(new ScanRequest("u", SessionType.TRAINING, 3L, 2L, null, null, 9L));

        // Assert
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionService).save(captor.capture());
        assertEquals(9L, captor.getValue().getId());
    }

    @Test
    void scan_rejectsAlreadyImportedResult() {
        // Arrange
        when(disagQrService.decode("u")).thenReturn(null);
        when(disagQrService.toSession(null)).thenReturn(scanned());
        when(sessionService.existsByUserAndExternalId(testUser, "uuid-1")).thenReturn(true);

        // Act
        ResponseEntity<?> response = controller.scan(new ScanRequest("u", SessionType.TRAINING, 3L, 2L, null, null, null));

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(sessionService, never()).save(any());
    }
}
