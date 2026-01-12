package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
}