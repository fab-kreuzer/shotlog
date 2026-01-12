package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTest {

    @Test
    void testGetShotSumWhenSeriesIsEmpty() {
        // Arrange
        Session session = new Session();
        session.setSeries(List.of());

        // Act
        double result = session.getShotSum();

        // Assert
        assertEquals(0.0, result, "Shot sum should be 0 when series list is empty");
    }

    @Test
    void testGetShotSumWhenSeriesHasValues() {
        // Arrange
        Session session = new Session();
        Series series1 = new Series();
        Series series2 = new Series();

        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(10.0));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(9.5));
        Shot shot3 = new Shot();
        shot3.setValue(BigDecimal.valueOf(9.0));

        Shot shot4 = new Shot();
        shot4.setValue(BigDecimal.valueOf(8.0));
        Shot shot5 = new Shot();
        shot5.setValue(BigDecimal.valueOf(10.0));

        series1.setShots(List.of(shot1, shot2, shot3));
        series2.setShots(List.of(shot4, shot5));

        session.setSeries(List.of(series1, series2));

        // Act
        double result = session.getShotSum();

        // Assert
        assertEquals(46.5, result, 0.001, "Shot sum should correctly calculate the total shots sum");
    }

    @Test
    void testGetShotSumWithMixedSeries() {
        // Arrange
        Session session = new Session();
        Series series1 = new Series();
        Series series2 = new Series();
        Series series3 = new Series();

        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(10.0));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(9.0));

        Shot shot3 = new Shot(); // Empty series

        Shot shot4 = new Shot();
        shot4.setValue(BigDecimal.valueOf(7.0));
        Shot shot5 = new Shot();
        shot5.setValue(BigDecimal.valueOf(6.5));
        Shot shot6 = new Shot();
        shot6.setValue(BigDecimal.valueOf(9.0));

        series1.setShots(List.of(shot1, shot2));
        series2.setShots(List.of());
        series3.setShots(List.of(shot4, shot5, shot6));

        session.setSeries(List.of(series1, series2, series3));

        // Act
        double result = session.getShotSum();

        // Assert
        assertEquals(41.5, result, 0.001, "Shot sum should correctly calculate total even with empty or mixed series");
    }
}