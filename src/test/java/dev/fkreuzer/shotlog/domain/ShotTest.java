package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ShotTest {

    @Test
    void setAndGetValue() {
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(9.5));
        assertEquals(BigDecimal.valueOf(9.5), shot.getValue());
    }

    @Test
    void setAndGetShotNumber() {
        Shot shot = new Shot();
        shot.setShotNumber(3);
        assertEquals(3, shot.getShotNumber());
    }

    @Test
    void setAndGetSeries() {
        Shot shot = new Shot();
        Series series = new Series();
        shot.setSeries(series);
        assertSame(series, shot.getSeries());
    }

    @Test
    void defaultValueIsNull() {
        Shot shot = new Shot();
        assertNull(shot.getValue());
        assertNull(shot.getId());
        assertEquals(0, shot.getShotNumber());
    }
}
