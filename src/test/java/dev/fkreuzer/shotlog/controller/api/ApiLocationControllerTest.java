package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiLocationControllerTest {

    @Mock
    private ShootingPlaceRepository shootingPlaceRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ApiLocationController controller;

    @Test
    void getLocations_shouldReturnAllLocations() {
        // Arrange
        ShootingPlace place1 = new ShootingPlace();
        place1.setClub("Club A");
        ShootingPlace place2 = new ShootingPlace();
        place2.setClub("Club B");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(place1, place2));

        // Act
        ResponseEntity<List<ShootingPlace>> response = controller.getLocations();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(2, response.getBody()
                .size());
        verify(shootingPlaceRepository).findAll();
    }

    @Test
    void getLocations_shouldReturnEmptyList_whenNoLocationsExist() {
        // Arrange
        when(shootingPlaceRepository.findAll()).thenReturn(List.of());

        // Act
        ResponseEntity<List<ShootingPlace>> response = controller.getLocations();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertTrue(response.getBody()
                .isEmpty());
    }

    // ---- createLocation ----

    @Test
    void createLocation_shouldCreate_whenValid() {
        // Act
        ResponseEntity<ApiResponse> response =
                controller.createLocation(Map.of("club", "New Club", "location", "Munich"));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository).save(argMatchingClubLocation("New Club", "Munich"));
    }

    @Test
    void createLocation_shouldReturnBadRequest_whenClubMissing() {
        // Act
        ResponseEntity<ApiResponse> response =
                controller.createLocation(Map.of("location", "Munich"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).save(any());
    }

    @Test
    void createLocation_shouldReturnBadRequest_whenLocationMissing() {
        // Act
        ResponseEntity<ApiResponse> response =
                controller.createLocation(Map.of("club", "New Club"));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).save(any());
    }

    // ---- updateLocation ----

    @Test
    void updateLocation_shouldUpdate_whenValid() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setClub("Old");
        place.setLocation("Old City");
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));

        // Act
        ResponseEntity<ApiResponse> response =
                controller.updateLocation(1L, Map.of("club", "New", "location", "New City"));

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals("New", place.getClub());
        assertEquals("New City", place.getLocation());
        verify(shootingPlaceRepository).save(place);
    }

    @Test
    void updateLocation_shouldReturn404_whenNotFound() {
        // Arrange
        when(shootingPlaceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse> response =
                controller.updateLocation(99L, Map.of("club", "New"));

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).save(any());
    }

    @Test
    void updateLocation_shouldReturnBadRequest_whenClubBlank() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));

        // Act
        ResponseEntity<ApiResponse> response =
                controller.updateLocation(1L, Map.of("club", " "));

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).save(any());
    }

    // ---- deleteLocation ----

    @Test
    void deleteLocation_shouldDelete_whenNotInUse() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setClub("Club A");
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));
        when(sessionRepository.countByEnemyId(1L)).thenReturn(0L);
        when(userAccountRepository.countByHomeClubId(1L)).thenReturn(0L);

        // Act
        ResponseEntity<ApiResponse> response = controller.deleteLocation(1L);

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository).deleteById(1L);
    }

    @Test
    void deleteLocation_shouldReturn404_whenNotFound() {
        // Arrange
        when(shootingPlaceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse> response = controller.deleteLocation(99L);

        // Assert
        assertEquals(404, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).deleteById(any());
    }

    @Test
    void deleteLocation_shouldReturnBadRequest_whenUsedBySessions() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setClub("Club A");
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));
        when(sessionRepository.countByEnemyId(1L)).thenReturn(3L);

        // Act
        ResponseEntity<ApiResponse> response = controller.deleteLocation(1L);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).deleteById(any());
    }

    @Test
    void deleteLocation_shouldReturnBadRequest_whenUsedAsHomeClub() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setClub("Club A");
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));
        when(sessionRepository.countByEnemyId(1L)).thenReturn(0L);
        when(userAccountRepository.countByHomeClubId(1L)).thenReturn(2L);

        // Act
        ResponseEntity<ApiResponse> response = controller.deleteLocation(1L);

        // Assert
        assertEquals(400, response.getStatusCode()
                .value());
        verify(shootingPlaceRepository, never()).deleteById(any());
    }

    private static ShootingPlace argMatchingClubLocation(String club, String location) {
        return org.mockito.ArgumentMatchers.argThat(place ->
                club.equals(place.getClub()) && location.equals(place.getLocation()));
    }
}
