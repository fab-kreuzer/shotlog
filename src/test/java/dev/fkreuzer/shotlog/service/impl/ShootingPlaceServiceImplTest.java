package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShootingPlaceServiceImplTest {

    @Mock
    private ShootingPlaceRepository shootingPlaceRepository;

    @InjectMocks
    private ShootingPlaceServiceImpl shootingPlaceService;

    @Test
    void findAll_shouldReturnAllPlaces() {
        // Arrange
        ShootingPlace place1 = new ShootingPlace();
        place1.setClub("Club A");
        ShootingPlace place2 = new ShootingPlace();
        place2.setClub("Club B");
        when(shootingPlaceRepository.findAll()).thenReturn(List.of(place1, place2));

        // Act
        List<ShootingPlace> result = shootingPlaceService.findAll();

        // Assert
        assertEquals(2, result.size());
        verify(shootingPlaceRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoPlacesExist() {
        // Arrange
        when(shootingPlaceRepository.findAll()).thenReturn(List.of());

        // Act
        List<ShootingPlace> result = shootingPlaceService.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnPlace_whenFound() {
        // Arrange
        ShootingPlace place = new ShootingPlace();
        place.setId(1L);
        place.setClub("Test Club");
        when(shootingPlaceRepository.findById(1L)).thenReturn(Optional.of(place));

        // Act
        ShootingPlace result = shootingPlaceService.findById(1L);

        // Assert
        assertEquals("Test Club", result.getClub());
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldReturnNewShootingPlace_whenNotFound() {
        // Arrange
        when(shootingPlaceRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ShootingPlace result = shootingPlaceService.findById(99L);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getClub());
        assertNull(result.getLocation());
    }
}
