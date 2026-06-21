package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiLocationControllerTest {

    @Mock
    private ShootingPlaceService shootingPlaceService;

    @InjectMocks
    private ApiLocationController controller;

    @Test
    void getLocations_shouldReturnAllLocations() {
        // Arrange
        ShootingPlace place1 = new ShootingPlace();
        place1.setClub("Club A");
        ShootingPlace place2 = new ShootingPlace();
        place2.setClub("Club B");
        when(shootingPlaceService.findAll()).thenReturn(List.of(place1, place2));

        // Act
        ResponseEntity<List<ShootingPlace>> response = controller.getLocations();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertEquals(2, response.getBody()
                .size());
        verify(shootingPlaceService).findAll();
    }

    @Test
    void getLocations_shouldReturnEmptyList_whenNoLocationsExist() {
        // Arrange
        when(shootingPlaceService.findAll()).thenReturn(List.of());

        // Act
        ResponseEntity<List<ShootingPlace>> response = controller.getLocations();

        // Assert
        assertEquals(200, response.getStatusCode()
                .value());
        assertTrue(response.getBody()
                .isEmpty());
    }
}
