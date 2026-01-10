package dev.fkreuzer.shotlog.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTest {

    @Test
    void testGetShotSumWithEmptySeries() {
        Session session = new Session();
        session.setSeries(List.of());

        double shotSum = session.getShotSum();

        assertEquals(0.0, shotSum, "Shot sum should be 0.0 when series is empty");
    }

    @Test
    void testGetShotSumWithSingleSeries() {
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(10.0));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(9.5));
        Shot shot3 = new Shot();
        shot3.setValue(BigDecimal.valueOf(8.0));

        Series series = new Series();
        series.setShots(List.of(shot1, shot2, shot3));
        Session session = new Session();
        session.setSeries(List.of(series));

        double shotSum = session.getShotSum();

        assertEquals(27.5, shotSum, 0.001, "Shot sum should correctly sum up all shots in the single series");
    }

    @Test
    void testGetShotSumWithMultipleSeries() {
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(10.0));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(9.0));

        Series series1 = new Series();
        series1.setShots(List.of(shot1, shot2));

        Shot shot3 = new Shot();
        shot3.setValue(BigDecimal.valueOf(8.5));
        Shot shot4 = new Shot();
        shot4.setValue(BigDecimal.valueOf(7.5));

        Series series2 = new Series();
        series2.setShots(List.of(shot3, shot4));

        Shot shot5 = new Shot();
        shot5.setValue(BigDecimal.valueOf(10.0));
        Shot shot6 = new Shot();
        shot6.setValue(BigDecimal.valueOf(10.0));
        Shot shot7 = new Shot();
        shot7.setValue(BigDecimal.valueOf(10.0));

        Series series3 = new Series();
        series3.setShots(List.of(shot5, shot6, shot7));

        Session session = new Session();
        session.setSeries(List.of(series1, series2, series3));

        double shotSum = session.getShotSum();

        assertEquals(65.0, shotSum, 0.001, "Shot sum should correctly sum up all shots across multiple series");
    }

    @Test
    void testGetShotSumWithDecimalScoring() {
        Shot shot1 = new Shot();
        shot1.setValue(BigDecimal.valueOf(10.0));
        Shot shot2 = new Shot();
        shot2.setValue(BigDecimal.valueOf(9.9));
        Shot shot3 = new Shot();
        shot3.setValue(BigDecimal.valueOf(9.8));

        Series series1 = new Series();
        series1.setShots(List.of(shot1, shot2, shot3));

        Shot shot4 = new Shot();
        shot4.setValue(BigDecimal.valueOf(8.7));
        Shot shot5 = new Shot();
        shot5.setValue(BigDecimal.valueOf(7.6));

        Series series2 = new Series();
        series2.setShots(List.of(shot4, shot5));

        Session session = new Session();
        session.setSeries(List.of(series1, series2));

        double shotSum = session.getShotSum();

        assertEquals(46.0, shotSum, 0.001, "Shot sum should handle decimal scoring accurately");
    }
}