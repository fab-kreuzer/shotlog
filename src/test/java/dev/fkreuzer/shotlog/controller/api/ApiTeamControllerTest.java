package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.domain.datatypes.TeamRole;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.repository.UserTeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTeamControllerTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private UserTeamRepository userTeamRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @InjectMocks
    private ApiTeamController controller;

    private UserAccount userWithId(long id) {
        UserAccount user = new UserAccount("user" + id, "hash", Set.of(new Role("USER")));
        user.setId(id);
        return user;
    }

    private Team teamWithId(long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        team.setUserTeams(new ArrayList<>());
        return team;
    }

    // --- getTeams ---

    @Test
    void getTeams_shouldReturnAllTeams_whenNoSeasonGiven() {
        when(teamRepository.findAll()).thenReturn(List.of(teamWithId(1, "A"), teamWithId(2, "B")));

        List<Team> result = controller.getTeams(null);

        assertEquals(2, result.size());
    }

    @Test
    void getTeams_shouldReturnSeasonTeams_whenSeasonGiven() {
        Season season = new Season("2025/26");
        season.setId(5L);
        when(seasonRepository.findById(5L)).thenReturn(Optional.of(season));
        when(teamRepository.findAllBySeason(season)).thenReturn(List.of(teamWithId(1, "A")));

        List<Team> result = controller.getTeams(5L);

        assertEquals(1, result.size());
        verify(teamRepository, never()).findAll();
    }

    // --- getTeamRoles ---

    @Test
    void getTeamRoles_shouldReturnEveryRoleWithNameAndType() {
        List<Map<String, String>> roles = controller.getTeamRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains(Map.of("name", "MEMBER", "type", "Mitglied")));
        assertTrue(roles.contains(Map.of("name", "LEADER", "type", "Leiter")));
    }

    // --- getAssignedTeams ---

    @Test
    void getAssignedTeams_shouldReturnTeams_whenUserHasMemberships() {
        UserAccount user = userWithId(1);
        Team team = teamWithId(10, "Alpha");
        UserTeam membership = new UserTeam();
        membership.setUser(user);
        membership.setTeam(team);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userTeamRepository.findUserTeamsByUser(user)).thenReturn(List.of(membership));

        List<Team> result = controller.getAssignedTeams(1L);

        assertEquals(1, result.size());
        assertSame(team, result.get(0));
    }

    @Test
    void getAssignedTeams_shouldReturnEmpty_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<Team> result = controller.getAssignedTeams(99L);

        assertTrue(result.isEmpty());
        verify(userTeamRepository, never()).findUserTeamsByUser(any());
    }

    @Test
    void getAssignedTeams_shouldReturnEmpty_whenUserHasNoTeams() {
        UserAccount user = userWithId(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userTeamRepository.findUserTeamsByUser(user)).thenReturn(List.of());

        List<Team> result = controller.getAssignedTeams(1L);

        assertTrue(result.isEmpty());
    }

    // --- createTeam ---

    @Test
    void createTeam_shouldSaveAndReturnTeam_underActiveSeason() {
        Season season = new Season("2025/26");
        season.setId(5L);
        when(seasonRepository.findByActiveTrue()).thenReturn(Optional.of(season));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = controller.createTeam(Map.<String, Object>of("name", "New Team"));

        assertEquals("New Team", result.getName());
        assertSame(season, result.getSeason());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_shouldUseGivenSeason_whenSeasonIdProvided() {
        Season season = new Season("2024/25");
        season.setId(3L);
        when(seasonRepository.findById(3L)).thenReturn(Optional.of(season));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Team");
        request.put("seasonId", 3);

        Team result = controller.createTeam(request);

        assertSame(season, result.getSeason());
        verify(seasonRepository, never()).findByActiveTrue();
    }

    @Test
    void createTeam_shouldThrowBadRequest_whenNameMissing() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createTeam(new HashMap<>()));

        assertEquals(400, ex.getStatusCode().value());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void createTeam_shouldThrowBadRequest_whenNameBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createTeam(Map.<String, Object>of("name", "   ")));

        assertEquals(400, ex.getStatusCode().value());
    }

    // --- addMemberToTeam ---

    @Test
    void addMemberToTeam_shouldDefaultToMemberRole_whenRoleAbsent() {
        Team team = teamWithId(1, "Alpha");
        UserAccount user = userWithId(2);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userTeamRepository.save(any(UserTeam.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);

        UserTeam result = controller.addMemberToTeam(1L, request);

        assertEquals(TeamRole.MEMBER, result.getRole());
        assertSame(user, result.getUser());
        assertSame(team, result.getTeam());
    }

    @Test
    void addMemberToTeam_shouldUseProvidedRole() {
        Team team = teamWithId(1, "Alpha");
        UserAccount user = userWithId(2);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userTeamRepository.save(any(UserTeam.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);
        request.put("role", "LEADER");

        UserTeam result = controller.addMemberToTeam(1L, request);

        assertEquals(TeamRole.LEADER, result.getRole());
    }

    @Test
    void addMemberToTeam_shouldThrowBadRequest_whenRoleInvalid() {
        Team team = teamWithId(1, "Alpha");
        UserAccount user = userWithId(2);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);
        request.put("role", "CAPTAIN");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.addMemberToTeam(1L, request));

        assertEquals(400, ex.getStatusCode().value());
        verify(userTeamRepository, never()).save(any());
    }

    @Test
    void addMemberToTeam_shouldThrowNotFound_whenTeamMissing() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.addMemberToTeam(1L, request));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void addMemberToTeam_shouldThrowNotFound_whenUserMissing() {
        Team team = teamWithId(1, "Alpha");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.addMemberToTeam(1L, request));

        assertEquals(404, ex.getStatusCode().value());
    }

    // --- removeMemberFromTeam ---

    @Test
    void removeMemberFromTeam_shouldDetachMembershipFromBothSides() {
        UserAccount user = userWithId(2);
        Team team = teamWithId(1, "Alpha");

        UserTeam membership = new UserTeam();
        membership.setUser(user);
        membership.setTeam(team);

        List<UserTeam> teamMemberships = new ArrayList<>(List.of(membership));
        team.setUserTeams(teamMemberships);
        List<UserTeam> userMemberships = new ArrayList<>(List.of(membership));
        user.setTeams(userMemberships);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        controller.removeMemberFromTeam(1L, 2L);

        assertTrue(team.getUserTeams().isEmpty());
        assertTrue(user.getTeams().isEmpty());
    }

    @Test
    void removeMemberFromTeam_shouldThrowNotFound_whenTeamMissing() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.removeMemberFromTeam(1L, 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void removeMemberFromTeam_shouldThrowNotFound_whenMemberNotInTeam() {
        Team team = teamWithId(1, "Alpha");
        team.setUserTeams(new ArrayList<>());
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.removeMemberFromTeam(1L, 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    // --- deleteTeam ---

    @Test
    void deleteTeam_shouldDeleteTeam_whenFound() {
        Team team = teamWithId(1, "Alpha");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(sessionRepository.findAllByTeam(team)).thenReturn(List.of());

        controller.deleteTeam(1L, true);

        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_shouldThrowNotFound_whenMissing() {
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.deleteTeam(1L, false));

        assertEquals(404, ex.getStatusCode().value());
        verify(teamRepository, never()).delete(any());
    }
}