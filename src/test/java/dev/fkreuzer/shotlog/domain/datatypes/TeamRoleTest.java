package dev.fkreuzer.shotlog.domain.datatypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamRoleTest {

    @Test
    void getType_shouldReturnMitglied_forMember() {
        assertEquals("Mitglied", TeamRole.MEMBER.getType());
    }

    @Test
    void getType_shouldReturnLeiter_forLeader() {
        assertEquals("Leiter", TeamRole.LEADER.getType());
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(TeamRole.MEMBER, TeamRole.valueOf("MEMBER"));
        assertEquals(TeamRole.LEADER, TeamRole.valueOf("LEADER"));
    }

    @Test
    void valueOf_shouldThrow_forUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> TeamRole.valueOf("CAPTAIN"));
    }

    @Test
    void values_shouldContainExactlyTwoEntries() {
        assertEquals(2, TeamRole.values().length);
    }
}
