package dev.fkreuzer.shotlog.domain;

import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // --- getShotSumOfTestShots ---

    @Test
    void testGetShotSumOfTestShots_onlyTestShotSeriesAreSummed() {
        Session session = new Session();

        Series testSeries = new Series();
        testSeries.setTestShot(true);
        Shot ts1 = new Shot();
        ts1.setValue(BigDecimal.valueOf(8.0));
        testSeries.setShots(List.of(ts1));

        Series normalSeries = new Series();
        normalSeries.setTestShot(false);
        Shot ns1 = new Shot();
        ns1.setValue(BigDecimal.valueOf(10.0));
        normalSeries.setShots(List.of(ns1));

        session.setSeries(List.of(testSeries, normalSeries));

        assertEquals(8.0, session.getShotSumOfTestShots(), 0.001);
    }

    // --- getFormattedShotSum ---

    @Test
    void testGetFormattedShotSum_decimalScoring() {
        Session session = new Session();
        session.setDecimalScoring(true);
        Series s = new Series();
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(9.5));
        s.setShots(List.of(shot));
        session.setSeries(List.of(s));

        String result = session.getFormattedShotSum();
        // Locale-dependent: may use '.' or ',' as decimal separator
        assertTrue(result.equals("9.5") || result.equals("9,5"), "Expected 9.5 or 9,5 but was: " + result);
    }

    @Test
    void testGetFormattedShotSum_integerScoring() {
        Session session = new Session();
        session.setDecimalScoring(false);
        Series s = new Series();
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(9.5));
        s.setShots(List.of(shot));
        session.setSeries(List.of(s));

        String result = session.getFormattedShotSum();
        assertTrue(result.equals("10") || result.equals("10"), "Expected 10 but was: " + result);
    }

    // --- getFormattedShotSumOfTestShots ---

    @Test
    void testGetFormattedShotSumOfTestShots_decimalScoring() {
        Session session = new Session();
        session.setDecimalScoring(true);
        Series s = new Series();
        s.setTestShot(true);
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(7.3));
        s.setShots(List.of(shot));
        session.setSeries(List.of(s));

        String result = session.getFormattedShotSumOfTestShots();
        assertTrue(result.equals("7.3") || result.equals("7,3"), "Expected 7.3 or 7,3 but was: " + result);
    }

    @Test
    void testGetFormattedShotSumOfTestShots_integerScoring() {
        Session session = new Session();
        session.setDecimalScoring(false);
        Series s = new Series();
        s.setTestShot(true);
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(7.3));
        s.setShots(List.of(shot));
        session.setSeries(List.of(s));

        String result = session.getFormattedShotSumOfTestShots();
        assertEquals("7", result);
    }

    // --- getTranslatedLocation ---

    @Test
    void testGetTranslatedLocation() {
        Session session = new Session();
        ShootingPlace place = new ShootingPlace();
        place.setClub("SV Musterstadt");
        session.setEnemy(place);

        assertEquals("SV Musterstadt", session.getTranslatedLocation());
    }

    // --- getFormattedType ---

    @Test
    void testGetFormattedType_training() {
        Session session = new Session();
        session.setSessionType(SessionType.TRAINING);

        assertEquals("Training", session.getFormattedType());
    }

    @Test
    void testGetFormattedType_competition() {
        Session session = new Session();
        session.setSessionType(SessionType.COMPETITION);

        assertEquals("Wettkampf", session.getFormattedType());
    }
}