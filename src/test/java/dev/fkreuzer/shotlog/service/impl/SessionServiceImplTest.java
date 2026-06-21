package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.Series;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Shot;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private UserAccount createUser() {
        return new UserAccount("user", "hash", Set.of());
    }

    // --- save: new session ---

    @Test
    void save_newSession_shouldSetSeriesAndShotReferences() {
        // Arrange
        Session session = new Session();
        // No ID means new session

        Shot shot = new Shot();
        shot.setShotNumber(1);
        shot.setValue(BigDecimal.TEN);

        Series series = new Series();
        series.setSeriesNumber(1);
        series.setShots(new ArrayList<>(List.of(shot)));

        session.setSeries(new ArrayList<>(List.of(series)));

        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        Session result = sessionService.save(session);

        // Assert
        assertSame(session, series.getSession());
        assertSame(series, shot.getSeries());
        verify(sessionRepository).save(session);
    }

    @Test
    void save_newSession_withNullShots_shouldNotThrow() {
        // Arrange
        Session session = new Session();
        Series series = new Series();
        series.setSeriesNumber(1);
        series.setShots(null);
        session.setSeries(new ArrayList<>(List.of(series)));

        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        Session result = sessionService.save(session);

        // Assert
        assertSame(session, series.getSession());
        verify(sessionRepository).save(session);
    }

    // --- save: update existing session ---

    @Test
    void save_existingSession_shouldPreserveIdsForMatchingSeries() {
        // Arrange
        Session existingSession = new Session();
        existingSession.setId(1L);

        Shot existingShot = new Shot();
        existingShot.setId(100L);
        existingShot.setShotNumber(1);

        Series existingSeries = new Series();
        existingSeries.setId(10L);
        existingSeries.setSeriesNumber(1);
        existingSeries.setShots(new ArrayList<>(List.of(existingShot)));

        existingSession.setSeries(new ArrayList<>(List.of(existingSeries)));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(existingSession));

        // Updated session
        Session updatedSession = new Session();
        updatedSession.setId(1L);

        Shot updatedShot = new Shot();
        updatedShot.setShotNumber(1);
        updatedShot.setValue(BigDecimal.valueOf(9.5));

        Series updatedSeries = new Series();
        updatedSeries.setSeriesNumber(1);
        updatedSeries.setShots(new ArrayList<>(List.of(updatedShot)));

        updatedSession.setSeries(new ArrayList<>(List.of(updatedSeries)));

        when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);

        // Act
        sessionService.save(updatedSession);

        // Assert
        assertEquals(10L, updatedSeries.getId());
        assertEquals(100L, updatedShot.getId());
        assertSame(updatedSession, updatedSeries.getSession());
        assertSame(updatedSeries, updatedShot.getSeries());
    }

    @Test
    void save_existingSession_withNewSeries_shouldSetReferences() {
        // Arrange
        Session existingSession = new Session();
        existingSession.setId(1L);
        existingSession.setSeries(new ArrayList<>());

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(existingSession));

        Session updatedSession = new Session();
        updatedSession.setId(1L);

        Shot newShot = new Shot();
        newShot.setShotNumber(1);

        Series newSeries = new Series();
        newSeries.setSeriesNumber(2);
        newSeries.setShots(new ArrayList<>(List.of(newShot)));

        updatedSession.setSeries(new ArrayList<>(List.of(newSeries)));

        when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);

        // Act
        sessionService.save(updatedSession);

        // Assert
        assertSame(updatedSession, newSeries.getSession());
        assertSame(newSeries, newShot.getSeries());
        assertNull(newSeries.getId());
    }

    @Test
    void save_existingSession_notFoundInDb_shouldJustSave() {
        // Arrange
        Session session = new Session();
        session.setId(999L);
        session.setSeries(new ArrayList<>());

        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        // Act
        Session result = sessionService.save(session);

        // Assert
        verify(sessionRepository).save(session);
    }

    // --- findAllByUserAndType ---

    @Test
    void findAllByUserAndType_shouldDelegateToRepository() {
        // Arrange
        UserAccount user = createUser();
        List<Session> expected = List.of(new Session());
        when(sessionRepository.findAllByUserAndSessionTypeOrderBySessionDateAscSessionTimeAsc(user, SessionType.TRAINING))
                .thenReturn(expected);

        // Act
        List<Session> result = sessionService.findAllByUserAndType(user, SessionType.TRAINING);

        // Assert
        assertSame(expected, result);
        verify(sessionRepository).findAllByUserAndSessionTypeOrderBySessionDateAscSessionTimeAsc(user, SessionType.TRAINING);
    }

    // --- findByIdAndUser ---

    @Test
    void findByIdAndUser_shouldDelegateToRepository() {
        // Arrange
        UserAccount user = createUser();
        Session session = new Session();
        when(sessionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(session));

        // Act
        Optional<Session> result = sessionService.findByIdAndUser(1L, user);

        // Assert
        assertTrue(result.isPresent());
        assertSame(session, result.get());
    }

    @Test
    void findByIdAndUser_shouldReturnEmpty_whenNotFound() {
        // Arrange
        UserAccount user = createUser();
        when(sessionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        // Act
        Optional<Session> result = sessionService.findByIdAndUser(99L, user);

        // Assert
        assertTrue(result.isEmpty());
    }

    // --- deleteByIdAndUser ---

    @Test
    void deleteByIdAndUser_shouldDelegateToRepository() {
        // Arrange
        UserAccount user = createUser();

        // Act
        sessionService.deleteByIdAndUser(1L, user);

        // Assert
        verify(sessionRepository).deleteByIdAndUser(1L, user);
    }

    // --- findAllByUser ---

    @Test
    void findAllByUser_shouldDelegateToRepository() {
        // Arrange
        UserAccount user = createUser();
        List<Session> expected = List.of(new Session(), new Session());
        when(sessionRepository.findAllByUser(user)).thenReturn(expected);

        // Act
        List<Session> result = sessionService.findAllByUser(user);

        // Assert
        assertSame(expected, result);
        verify(sessionRepository).findAllByUser(user);
    }
}
