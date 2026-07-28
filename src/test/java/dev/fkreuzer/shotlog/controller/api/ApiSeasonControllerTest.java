package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSeasonControllerTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ApiSeasonController controller;

    private Season seasonWithId(long id, boolean active) {
        Season season = new Season("Season " + id);
        season.setId(id);
        season.setActive(active);
        return season;
    }

    // --- getSeasons ---

    @Test
    void getSeasons_shouldReturnAllSeasons() {
        when(seasonRepository.findAll()).thenReturn(List.of(seasonWithId(1, true), seasonWithId(2, false)));

        List<Season> result = controller.getSeasons();

        assertEquals(2, result.size());
    }

    // --- setActiveSeason ---

    @Test
    void setActiveSeason_shouldReturn404_whenSeasonNotFound() {
        when(seasonRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> response = controller.setActiveSeason(99L);

        assertEquals(404, response.getStatusCode().value());
        verify(seasonRepository, never()).saveAndFlush(any());
    }

    @Test
    void setActiveSeason_shouldActivateTarget_andDeactivateCurrentlyActive() {
        Season target = seasonWithId(1, false);
        Season previouslyActive = seasonWithId(2, true);

        when(seasonRepository.findById(1L)).thenReturn(Optional.of(target));
        when(seasonRepository.findAll()).thenReturn(List.of(target, previouslyActive));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<ApiResponse> response = controller.setActiveSeason(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(target.isActive());
        assertFalse(previouslyActive.isActive());
        verify(seasonRepository).saveAndFlush(previouslyActive);
        verify(seasonRepository).saveAndFlush(target);
    }

    @Test
    void setActiveSeason_shouldNotDeactivateTargetItself_whenAlreadyActive() {
        Season target = seasonWithId(1, true);

        when(seasonRepository.findById(1L)).thenReturn(Optional.of(target));
        when(seasonRepository.findAll()).thenReturn(List.of(target));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<ApiResponse> response = controller.setActiveSeason(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(target.isActive());
        // Only the final activation save, no deactivation pass for the target itself.
        verify(seasonRepository, times(1)).saveAndFlush(target);
    }

    // --- createSeason ---

    @Test
    void createSeason_shouldSave_whenNameNew() {
        when(seasonRepository.findByDescription("2025/26")).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<ApiResponse> response = controller.createSeason(Map.of("description", "2025/26"));

        assertEquals(200, response.getStatusCode().value());
        verify(seasonRepository).save(any(Season.class));
    }

    @Test
    void createSeason_shouldReturn400_whenNameBlank() {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("required");

        ResponseEntity<ApiResponse> response = controller.createSeason(Map.of("description", "   "));

        assertEquals(400, response.getStatusCode().value());
        verify(seasonRepository, never()).save(any());
    }

    @Test
    void createSeason_shouldReturn409_whenNameDuplicate() {
        when(seasonRepository.findByDescription("2025/26")).thenReturn(Optional.of(seasonWithId(1, false)));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("duplicate");

        ResponseEntity<ApiResponse> response = controller.createSeason(Map.of("description", "2025/26"));

        assertEquals(409, response.getStatusCode().value());
        verify(seasonRepository, never()).save(any());
    }

    // --- updateSeason ---

    @Test
    void updateSeason_shouldUpdate_whenFound() {
        Season season = seasonWithId(1, false);
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(seasonRepository.findByDescription("Renamed")).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<ApiResponse> response = controller.updateSeason(1L, Map.of("description", "Renamed"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Renamed", season.getDescription());
        verify(seasonRepository).save(season);
    }

    @Test
    void updateSeason_shouldReturn404_whenMissing() {
        when(seasonRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> response = controller.updateSeason(9L, Map.of("description", "X"));

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void updateSeason_shouldReturn409_whenNameTakenByAnotherSeason() {
        Season season = seasonWithId(1, false);
        Season other = seasonWithId(2, false);
        other.setDescription("Taken");
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(seasonRepository.findByDescription("Taken")).thenReturn(Optional.of(other));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("duplicate");

        ResponseEntity<ApiResponse> response = controller.updateSeason(1L, Map.of("description", "Taken"));

        assertEquals(409, response.getStatusCode().value());
        verify(seasonRepository, never()).save(any());
    }

    // --- deleteSeason ---

    @Test
    void deleteSeason_shouldDelete_whenNotActiveAndUnused() {
        Season season = seasonWithId(1, false);
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(teamRepository.countBySeason(season)).thenReturn(0L);
        when(sessionRepository.countBySeason(season)).thenReturn(0L);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("ok");

        ResponseEntity<ApiResponse> response = controller.deleteSeason(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(seasonRepository).delete(season);
    }

    @Test
    void deleteSeason_shouldReturn409_whenActive() {
        Season season = seasonWithId(1, true);
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("active");

        ResponseEntity<ApiResponse> response = controller.deleteSeason(1L);

        assertEquals(409, response.getStatusCode().value());
        verify(seasonRepository, never()).delete(any());
    }

    @Test
    void deleteSeason_shouldReturn409_whenInUse() {
        Season season = seasonWithId(1, false);
        when(seasonRepository.findById(1L)).thenReturn(Optional.of(season));
        when(teamRepository.countBySeason(season)).thenReturn(2L);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("in use");

        ResponseEntity<ApiResponse> response = controller.deleteSeason(1L);

        assertEquals(409, response.getStatusCode().value());
        verify(seasonRepository, never()).delete(any());
    }

    @Test
    void deleteSeason_shouldReturn404_whenMissing() {
        when(seasonRepository.findById(9L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse> response = controller.deleteSeason(9L);

        assertEquals(404, response.getStatusCode().value());
    }
}
