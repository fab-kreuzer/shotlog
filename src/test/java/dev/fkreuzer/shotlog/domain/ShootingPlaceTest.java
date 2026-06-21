package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShootingPlaceTest {

    @Test
    void setAndGetClub() {
        ShootingPlace place = new ShootingPlace();
        place.setClub("SV Musterstadt");
        assertEquals("SV Musterstadt", place.getClub());
    }

    @Test
    void setAndGetLocation() {
        ShootingPlace place = new ShootingPlace();
        place.setLocation("Musterstadt");
        assertEquals("Musterstadt", place.getLocation());
    }

    @Test
    void setAndGetId() {
        ShootingPlace place = new ShootingPlace();
        place.setId(42L);
        assertEquals(42L, place.getId());
    }

    @Test
    void defaultValuesAreNull() {
        ShootingPlace place = new ShootingPlace();
        assertNull(place.getId());
        assertNull(place.getClub());
        assertNull(place.getLocation());
    }
}
