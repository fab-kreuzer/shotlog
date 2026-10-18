package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.config.DisagProperties;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.dto.DisagResult;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisagQrServiceImplTest {

    private final DisagQrServiceImpl service =
            new DisagQrServiceImpl(RestClient.create(), new DisagProperties("http://localhost:1/x", "secret"));

    @Test
    void decode_rejectsForeignLinks() {
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.decode("https://evil.example/?uuid=1"));
        assertThrows(IllegalArgumentException.class, () -> service.decode(null));
    }

    @Test
    void toSession_splitsIntoSeriesOfTenWithDecimalValuesAndLocalTime() {
        // Arrange
        List<DisagResult.Shot> shots = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            shots.add(new DisagResult.Shot(100 + i, 2, 2, i, -i, i == 0 ? 0 : 30 + i));
        }
        DisagResult result = new DisagResult("abc",
                new DisagResult.Data("abc", "2026-10-14T20:26:47Z", null, null, shots));

        // Act
        Session session = service.toSession(result);

        // Assert
        assertEquals(3, session.getSeries().size());
        assertEquals(5, session.getSeries().get(2).getShots().size());
        assertEquals("10.0", session.getSeries().get(0).getShots().get(0).getValue().toString());
        assertEquals("12.4", session.getSeries().get(2).getShots().get(4).getValue().toString());
        assertTrue(session.isDecimalScoring());
        assertEquals("2026-10-14", session.getSessionDate().toString());
        assertEquals("22:26:47", session.getSessionTime().toString());
        assertEquals("abc", session.getExternalId());
        assertEquals("lg", session.getDiskType());
        assertEquals(3, session.getSeries().get(0).getShots().get(3).getX());
        assertEquals(-3, session.getSeries().get(0).getShots().get(3).getY());
        assertEquals(0, session.getSeries().get(0).getShots().get(0).getSecondsSincePrevious());
        assertEquals(33, session.getSeries().get(0).getShots().get(3).getSecondsSincePrevious());
    }

    @Test
    void toSession_dropsUnusableShotsAndReadsDiskTypeFromMeta() {
        // Arrange
        List<DisagResult.Shot> shots = List.of(new DisagResult.Shot(100, 2, 2, 0, 0, 0), new DisagResult.Shot(90, 0, 2, 1, 1, 40));
        DisagResult result = new DisagResult("abc", new DisagResult.Data("abc", "2026-10-14T20:26:47Z", null,
                new DisagResult.Meta("KK"), shots));

        // Act
        Session session = service.toSession(result);

        // Assert
        assertEquals(1, session.getSeries().get(0).getShots().size());
        assertEquals("kk", session.getDiskType());
    }
}
