package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.dto.LeaderboardEntry;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.security.SecurityUser;
import dev.fkreuzer.shotlog.service.LeaderboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiLeaderboardControllerTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private ApiLeaderboardController controller;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserAccount("testUser", "hash", Set.of(new Role("USER")));
        testUser.setId(1L);
        SecurityUser securityUser = new SecurityUser(testUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Team teamWithId(long id) {
        Team team = new Team();
        team.setId(id);
        team.setUserTeams(new ArrayList<>());
        return team;
    }

    private UserTeam membershipOf(UserAccount user, Team team) {
        UserTeam userTeam = new UserTeam();
        userTeam.setUser(user);
        userTeam.setTeam(team);
        return userTeam;
    }

    private LeaderboardEntry entryFor(long userId) {
        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setUserId(userId);
        return entry;
    }

    // --- getLeaderboard ---

    @Test
    void getLeaderboard_shouldReturnEntries_whenCurrentUserIsMember() {
        // Arrange
        Team team = teamWithId(1L);
        UserAccount other = new UserAccount("other", "hash", Set.of(new Role("USER")));
        other.setId(2L);
        team.getUserTeams()
                .add(membershipOf(testUser, team));
        team.getUserTeams()
                .add(membershipOf(other, team));

        LeaderboardEntry currentUserEntry = entryFor(1L);
        LeaderboardEntry otherEntry = entryFor(2L);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(leaderboardService.buildLeaderboard(team)).thenReturn(List.of(currentUserEntry, otherEntry));

        // Act
        List<LeaderboardEntry> result = controller.getLeaderboard(1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(currentUserEntry.isCurrentUser());
        assertFalse(otherEntry.isCurrentUser());
    }

    @Test
    void getLeaderboard_shouldThrowNotFound_whenTeamMissing() {
        // Arrange
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getLeaderboard(1L));

        assertEquals(404, ex.getStatusCode()
                .value());
        verify(leaderboardService, never()).buildLeaderboard(any());
    }

    @Test
    void getLeaderboard_shouldThrowForbidden_whenCurrentUserNotMember() {
        // Arrange
        Team team = teamWithId(1L);
        UserAccount other = new UserAccount("other", "hash", Set.of(new Role("USER")));
        other.setId(2L);
        team.getUserTeams()
                .add(membershipOf(other, team));

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        // Act & Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getLeaderboard(1L));

        assertEquals(403, ex.getStatusCode()
                .value());
        verify(leaderboardService, never()).buildLeaderboard(any());
    }
}
