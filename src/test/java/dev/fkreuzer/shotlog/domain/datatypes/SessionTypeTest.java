package dev.fkreuzer.shotlog.domain.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTypeTest {

    @Test
    void getType_shouldReturnTraining_forTrainingEnum() {
        assertEquals("Training", SessionType.TRAINING.getType());
    }

    @Test
    void getType_shouldReturnWettkampf_forCompetitionEnum() {
        assertEquals("Wettkampf", SessionType.COMPETITION.getType());
    }

    @Test
    void toUrlFormat_shouldReturnLowercaseName_forTraining() {
        assertEquals("training", SessionType.TRAINING.toUrlFormat());
    }

    @Test
    void toUrlFormat_shouldReturnLowercaseName_forCompetition() {
        assertEquals("competition", SessionType.COMPETITION.toUrlFormat());
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(SessionType.TRAINING, SessionType.valueOf("TRAINING"));
        assertEquals(SessionType.COMPETITION, SessionType.valueOf("COMPETITION"));
    }

    @Test
    void values_shouldContainExactlyTwoEntries() {
        assertEquals(2, SessionType.values().length);
    }
}
