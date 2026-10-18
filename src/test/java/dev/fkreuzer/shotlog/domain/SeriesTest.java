package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeriesTest {

    @Test
    void calculateShotSum_ShouldReturn0_WhenTestShotIsTrue() {
        // Arrange
        Series series = new Series();
        series.setTestShot(true);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(7.5));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(6.3));
        series.setShots(Arrays.asList(shot1, shot2));

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void calculateShotSum_ShouldReturnSumOfShots_WhenTestShotIsFalse() {
        // Arrange
        Series series = new Series();
        series.setTestShot(false);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(7.5));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(6.3));
        series.setShots(Arrays.asList(shot1, shot2));

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(13.8, result, 0.001);
    }

    @Test
    void calculateShotSum_ShouldSkipNullShotValues() {
        // Arrange
        Series series = new Series();
        series.setTestShot(false);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(8.5));
        Shot shot2 = new Shot(); // Null value
        shot2.setValue(null);
        Shot shot3 = new Shot();
        shot3.setValue(BigDecimal.valueOf(5.4));
        series.setShots(Arrays.asList(shot1, shot2, shot3));

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(13.9, result, 0.001);
    }

    @Test
    void calculateShotSum_ShouldReturn0_WhenNoShotsArePresent() {
        // Arrange
        Series series = new Series();
        series.setTestShot(false);

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void calculateShotSum_ShouldReturn0_WhenShotValuesAreAllNull() {
        // Arrange
        Series series = new Series();
        series.setTestShot(false);
        Shot shot1 = new Shot();
        shot1.setValue(null);
        Shot shot2 = new Shot();
        shot2.setValue(null);
        series.setShots(Arrays.asList(shot1, shot2));

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(0, result);
    }

    // --- calculateShotSumForTestShots ---

    @Test
    void calculateShotSumForTestShots_ShouldReturnSum_WhenTestShotIsTrue() {
        // Arrange
        Series series = new Series();
        series.setTestShot(true);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(7.5));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(6.3));
        series.setShots(Arrays.asList(shot1, shot2));

        // Act
        double result = series.calculateShotSumForTestShots();

        // Assert
        assertEquals(13.8, result, 0.001);
    }

    @Test
    void calculateShotSumForTestShots_ShouldReturn0_WhenTestShotIsFalse() {
        // Arrange
        Series series = new Series();
        series.setTestShot(false);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(7.5));
        series.setShots(Arrays.asList(shot1));

        // Act
        double result = series.calculateShotSumForTestShots();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void calculateShotSumForTestShots_ShouldSkipNullValues() {
        // Arrange
        Series series = new Series();
        series.setTestShot(true);
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(8.0));
        Shot shot2 = new Shot();
        shot2.setValue(null);
        series.setShots(Arrays.asList(shot1, shot2));

        // Act
        double result = series.calculateShotSumForTestShots();

        // Assert
        assertEquals(8.0, result, 0.001);
    }

    @Test
    void calculateShotSumForTestShots_ShouldReturn0_WhenNoShots() {
        // Arrange
        Series series = new Series();
        series.setTestShot(true);

        // Act
        double result = series.calculateShotSumForTestShots();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void calculateShotSum_ShouldCountWholeRings_WhenSessionHasNoDecimalScoring() {
        // Arrange
        Session session = new Session();
        session.setDecimalScoring(false);
        Series series = new Series();
        series.setSession(session);
        Shot a = new Shot();
        a.setValue(new BigDecimal("9.6"));
        Shot b = new Shot();
        b.setValue(new BigDecimal("10.4"));
        series.setShots(new ArrayList<>(List.of(a, b)));

        // Act
        double result = series.calculateShotSum();

        // Assert
        assertEquals(19.0, result);
    }
}
