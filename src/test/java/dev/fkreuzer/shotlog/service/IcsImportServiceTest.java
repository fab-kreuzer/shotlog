package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IcsImportServiceTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private ShootingPlaceRepository shootingPlaceRepository;

    @InjectMocks
    private IcsImportService service;

    private UserAccount user() {
        return new UserAccount("user", "hash", Set.of());
    }

    private InputStream ics(String body) {
        String content = "BEGIN:VCALENDAR\r\n" + body + "END:VCALENDAR\r\n";
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private Session captureSavedSession() {
        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionService).save(captor.capture());
        return captor.getValue();
    }

    // --- import count / control flow ---

    @Test
    void importFromIcs_shouldImportSingleEvent_andReturnCount() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(1, imported);
        verify(sessionService).save(any(Session.class));
    }

    @Test
    void importFromIcs_shouldImportMultipleEvents() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:First
                LOCATION:Munich
                END:VEVENT
                BEGIN:VEVENT
                DTSTART:20240116T100000
                SUMMARY:Second
                LOCATION:Berlin
                END:VEVENT
                """);

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(2, imported);
        verify(sessionService, times(2)).save(any(Session.class));
    }

    @Test
    void importFromIcs_shouldReturnZero_whenNoEvents() throws IOException {
        // Arrange
        InputStream input = ics("");

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(0, imported);
        verify(sessionService, never()).save(any());
    }

    @Test
    void importFromIcs_shouldSkipEventWithoutDtStart() throws IOException {
        // Arrange
        InputStream input = ics("""
                BEGIN:VEVENT
                SUMMARY:No date
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(0, imported);
        verify(sessionService, never()).save(any());
    }

    @Test
    void importFromIcs_shouldSkipEventWithUnparseableDtStart() throws IOException {
        // Arrange
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:not-a-date
                SUMMARY:Broken
                END:VEVENT
                """);

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(0, imported);
        verify(sessionService, never()).save(any());
    }

    @Test
    void importFromIcs_shouldIgnoreLinesWithoutColonInsideEvent() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                THISLINEHASNOCOLON
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        int imported = service.importFromIcs(input, user());

        // Assert
        assertEquals(1, imported);
    }

    // --- date / time parsing ---

    @Test
    void importFromIcs_shouldParseLocalDateTime() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(LocalDate.of(2024, 1, 15), session.getSessionDate());
        assertEquals(LocalTime.of(14, 30, 0), session.getSessionTime());
    }

    @Test
    void importFromIcs_shouldParseAllDayEvent_withValueDateParam() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART;VALUE=DATE:20240115
                SUMMARY:All day
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(LocalDate.of(2024, 1, 15), session.getSessionDate());
        assertEquals(LocalTime.MIDNIGHT, session.getSessionTime());
    }

    @Test
    void importFromIcs_shouldParseAllDayEvent_withBareEightDigitDate() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115
                SUMMARY:All day
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(LocalDate.of(2024, 1, 15), session.getSessionDate());
        assertEquals(LocalTime.MIDNIGHT, session.getSessionTime());
    }

    @Test
    void importFromIcs_shouldParseUtcDateTime() throws IOException {
        // Arrange — midday UTC stays on the same calendar day for any realistic server zone
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T120000Z
                SUMMARY:UTC event
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(LocalDate.of(2024, 1, 15), session.getSessionDate());
    }

    // --- session type detection ---

    @Test
    void importFromIcs_shouldDetectCompetitionType_whenSummaryContainsRwk() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:RWK Wettkampf
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(SessionType.COMPETITION, session.getSessionType());
    }

    @Test
    void importFromIcs_shouldDetectCompetitionType_fromDescription() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Match
                DESCRIPTION:Heute RWK Runde
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(SessionType.COMPETITION, session.getSessionType());
    }

    @Test
    void importFromIcs_shouldDefaultToTrainingType() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Regular practice
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals(SessionType.TRAINING, session.getSessionType());
    }

    // --- place resolution ---

    @Test
    void importFromIcs_shouldReuseExistingPlace_whenLocationMatchesClub() throws IOException {
        // Arrange
        ShootingPlace existing = new ShootingPlace();
        existing.setId(5L);
        existing.setClub("Munich");
        existing.setLocation("Somewhere");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(existing));

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertSame(existing, session.getEnemy());
        verify(shootingPlaceRepository, never()).save(any());
    }

    @Test
    void importFromIcs_shouldCreateNewPlace_whenLocationDoesNotMatch() throws IOException {
        // Arrange
        when(shootingPlaceRepository.findAll()).thenReturn(List.of());
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Hamburg
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        ArgumentCaptor<ShootingPlace> placeCaptor = ArgumentCaptor.forClass(ShootingPlace.class);
        verify(shootingPlaceRepository).save(placeCaptor.capture());
        assertEquals("Hamburg", placeCaptor.getValue().getClub());
        assertEquals("Hamburg", placeCaptor.getValue().getLocation());
    }

    @Test
    void importFromIcs_shouldFallBackToFirstPlace_whenNoLocation() throws IOException {
        // Arrange
        ShootingPlace existing = new ShootingPlace();
        existing.setId(7L);
        existing.setClub("Default");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(existing));

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertSame(existing, session.getEnemy());
        verify(shootingPlaceRepository, never()).save(any());
    }

    @Test
    void importFromIcs_shouldCreatePlaceholderPlace_whenNoLocationAndNoPlaces() throws IOException {
        // Arrange
        when(shootingPlaceRepository.findAll()).thenReturn(List.of());
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        ArgumentCaptor<ShootingPlace> placeCaptor = ArgumentCaptor.forClass(ShootingPlace.class);
        verify(shootingPlaceRepository).save(placeCaptor.capture());
        assertEquals("Unbekannt", placeCaptor.getValue().getClub());
        assertEquals("Unbekannt", placeCaptor.getValue().getLocation());
    }

    // --- home club flag ---

    @Test
    void importFromIcs_shouldSetHomeTrue_whenEnemyMatchesHomeClub() throws IOException {
        // Arrange
        ShootingPlace homeClub = new ShootingPlace();
        homeClub.setId(5L);
        homeClub.setClub("Munich");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(homeClub));

        UserAccount user = user();
        user.setHomeClub(homeClub);

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user);

        // Assert
        Session session = captureSavedSession();
        assertTrue(session.isHome());
    }

    @Test
    void importFromIcs_shouldSetHomeFalse_whenEnemyDiffersFromHomeClub() throws IOException {
        // Arrange
        ShootingPlace homeClub = new ShootingPlace();
        homeClub.setId(1L);
        homeClub.setClub("HomeTown");
        ShootingPlace other = new ShootingPlace();
        other.setId(2L);
        other.setClub("Munich");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(homeClub, other));

        UserAccount user = user();
        user.setHomeClub(homeClub);

        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user);

        // Assert
        Session session = captureSavedSession();
        assertFalse(session.isHome());
    }

    @Test
    void importFromIcs_shouldSetHomeFalse_whenHomeClubNull() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertFalse(session.isHome());
    }

    // --- line unfolding & unescaping ---

    @Test
    void importFromIcs_shouldUnfoldFoldedLines() throws IOException {
        // Arrange — RFC 5545: a leading space continues the previous line
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Long titl
                 e here
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals("Long title here", session.getTitle());
    }

    @Test
    void importFromIcs_shouldUnescapeSummary() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Trial\\, Run\\; done
                LOCATION:Munich
                END:VEVENT
                """);

        // Act
        service.importFromIcs(input, user());

        // Assert
        Session session = captureSavedSession();
        assertEquals("Trial, Run; done", session.getTitle());
    }

    @Test
    void importFromIcs_shouldSetDefaultsOnImportedSession() throws IOException {
        // Arrange
        when(shootingPlaceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        InputStream input = ics("""
                BEGIN:VEVENT
                DTSTART:20240115T143000
                SUMMARY:Training
                LOCATION:Munich
                END:VEVENT
                """);

        UserAccount user = user();

        // Act
        service.importFromIcs(input, user);

        // Assert
        Session session = captureSavedSession();
        assertSame(user, session.getUser());
        assertFalse(session.isDecimalScoring());
        assertNotNull(session.getSeries());
        assertTrue(session.getSeries().isEmpty());
    }
}
