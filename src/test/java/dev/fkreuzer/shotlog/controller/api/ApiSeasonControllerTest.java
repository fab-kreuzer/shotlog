package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
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
}
