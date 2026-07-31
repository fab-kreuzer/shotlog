package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.*;
import dev.fkreuzer.shotlog.dto.LeaderboardEntry;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private LeaderboardServiceImpl service;

    private UserAccount userWithId(long id, String username, String displayName) {
        UserAccount user = new UserAccount(username, "hash", displayName, Set.of());
        user.setId(id);
        return user;
    }

    private UserTeam membershipOf(UserAccount user, Team team) {
        UserTeam userTeam = new UserTeam();
        userTeam.setUser(user);
        userTeam.setTeam(team);
        return userTeam;
    }

    private Team teamWithMembers(UserTeam... userTeams) {
        Team team = new Team();
        team.setId(1L);
        team.setUserTeams(new ArrayList<>(List.of(userTeams)));
        return team;
    }

    private Session sessionOf(UserAccount user, LocalDate date, LocalTime time, double shotSum) {
        Session session = new Session();
        session.setUser(user);
        session.setSessionDate(date);
        session.setSessionTime(time);
        Shot shot = new Shot();
        shot.setValue(BigDecimal.valueOf(shotSum));
        Series series = new Series();
        series.setShots(List.of(shot));
        session.setSeries(List.of(series));
        return session;
    }

    private LeaderboardEntry entryFor(List<LeaderboardEntry> entries, long userId) {
        return entries.stream()
                .filter(e -> e.getUserId()
                        .equals(userId))
                .findFirst()
                .orElseThrow();
    }

    // --- buildLeaderboard ---

    @Test
    void buildLeaderboard_shouldRankMembersByTotalShotSumDescending_andComputeGapToLeader() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        UserAccount bob = userWithId(2, "bob", "Bob B.");
        Team team = teamWithMembers(membershipOf(alice, null), membershipOf(bob, null));

        Session aliceSession1 = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 100);
        Session aliceSession2 = sessionOf(alice, LocalDate.of(2026, 1, 8), LocalTime.of(10, 0), 110);
        Session bobSession1 = sessionOf(bob, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 90);

        when(sessionRepository.findAllByTeam(team)).thenReturn(
                List.of(aliceSession1, aliceSession2, bobSession1));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry aliceEntry = entryFor(result, 1L);
        LeaderboardEntry bobEntry = entryFor(result, 2L);

        assertEquals(1, aliceEntry.getRank());
        assertEquals(210.0, aliceEntry.getTotalShotSum());
        assertEquals(0.0, aliceEntry.getGapToLeader());

        assertEquals(2, bobEntry.getRank());
        assertEquals(90.0, bobEntry.getTotalShotSum());
        assertEquals(120.0, bobEntry.getGapToLeader());
    }

    @Test
    void buildLeaderboard_shouldIncludeMemberWithZeroSessions() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        UserAccount carol = userWithId(3, "carol", null);
        Team team = teamWithMembers(membershipOf(alice, null), membershipOf(carol, null));

        Session aliceSession = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 100);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(aliceSession));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry carolEntry = entryFor(result, 3L);
        assertEquals(0, carolEntry.getSessionCount());
        assertEquals(0.0, carolEntry.getTotalShotSum());
        assertEquals(0.0, carolEntry.getAveragePerSession());
        assertEquals(0.0, carolEntry.getBestSessionShotSum());
        assertEquals("FLAT", carolEntry.getTrend());
    }

    @Test
    void buildLeaderboard_shouldMarkTrendUp_whenLastSessionAboveAverage() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        Team team = teamWithMembers(membershipOf(alice, null));

        Session earlier = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 100);
        Session later = sessionOf(alice, LocalDate.of(2026, 1, 8), LocalTime.of(10, 0), 110);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(earlier, later));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry aliceEntry = entryFor(result, 1L);
        assertEquals("UP", aliceEntry.getTrend());
    }

    @Test
    void buildLeaderboard_shouldMarkTrendDown_whenLastSessionBelowAverage() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        Team team = teamWithMembers(membershipOf(alice, null));

        Session earlier = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 110);
        Session later = sessionOf(alice, LocalDate.of(2026, 1, 8), LocalTime.of(10, 0), 100);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(earlier, later));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry aliceEntry = entryFor(result, 1L);
        assertEquals("DOWN", aliceEntry.getTrend());
    }

    @Test
    void buildLeaderboard_shouldMarkTrendFlat_whenLastSessionEqualsAverage() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        Team team = teamWithMembers(membershipOf(alice, null));

        Session onlySession = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 90);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(onlySession));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry aliceEntry = entryFor(result, 1L);
        assertEquals("FLAT", aliceEntry.getTrend());
    }

    @Test
    void buildLeaderboard_shouldBreakTiesByDisplayNameAscending_whenTotalShotSumEqual() {
        // Arrange
        UserAccount zack = userWithId(1, "zack", "Zack");
        UserAccount alice = userWithId(2, "alice", "Alice");
        Team team = teamWithMembers(membershipOf(zack, null), membershipOf(alice, null));

        Session zackSession = sessionOf(zack, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 100);
        Session aliceSession = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 100);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(zackSession, aliceSession));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        assertEquals("Alice", result.get(0)
                .getDisplayName());
        assertEquals(1, result.get(0)
                .getRank());
        assertEquals("Zack", result.get(1)
                .getDisplayName());
        assertEquals(2, result.get(1)
                .getRank());
    }

    @Test
    void buildLeaderboard_shouldFallBackToUsername_whenDisplayNameNullOrBlank() {
        // Arrange
        UserAccount noDisplayName = userWithId(1, "alice", null);
        UserAccount blankDisplayName = userWithId(2, "bob", "   ");
        Team team = teamWithMembers(membershipOf(noDisplayName, null), membershipOf(blankDisplayName, null));

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of());

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        assertEquals("alice", entryFor(result, 1L).getDisplayName());
        assertEquals("bob", entryFor(result, 2L).getDisplayName());
    }

    @Test
    void buildLeaderboard_shouldComputeSessionCountTotalAverageAndBest() {
        // Arrange
        UserAccount alice = userWithId(1, "alice", null);
        Team team = teamWithMembers(membershipOf(alice, null));

        Session session1 = sessionOf(alice, LocalDate.of(2026, 1, 1), LocalTime.of(10, 0), 95);
        Session session2 = sessionOf(alice, LocalDate.of(2026, 1, 8), LocalTime.of(10, 0), 105);

        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of(session1, session2));

        // Act
        List<LeaderboardEntry> result = service.buildLeaderboard(team);

        // Assert
        LeaderboardEntry aliceEntry = entryFor(result, 1L);
        assertEquals(2, aliceEntry.getSessionCount());
        assertEquals(200.0, aliceEntry.getTotalShotSum());
        assertEquals(100.0, aliceEntry.getAveragePerSession());
        assertEquals(105.0, aliceEntry.getBestSessionShotSum());
    }
}
