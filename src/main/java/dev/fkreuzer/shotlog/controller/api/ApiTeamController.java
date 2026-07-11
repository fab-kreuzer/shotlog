package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.repository.UserTeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiTeamController {

    private final TeamRepository teamRepository;
    private final UserAccountRepository userRepository;
    private final UserTeamRepository userTeamRepository;

    public ApiTeamController(TeamRepository teamRepository, UserAccountRepository userRepository, UserTeamRepository userTeamRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.userTeamRepository = userTeamRepository;
    }

    @GetMapping("/teams")
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    @PostMapping("/teams")
    public Team createTeam(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim()
                .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required");
        }

        Team team = new Team();
        team.setName(name);
        return teamRepository.save(team);
    }

    @PostMapping("/teams/{teamId}/members")
    public UserTeam addMemberToTeam(@PathVariable Long teamId, @RequestBody Map<String, Object> request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        Long userId = ((Number) request.get("userId")).longValue();
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String role = (String) request.getOrDefault("role", "MEMBER");

        UserTeam userTeam = new UserTeam();
        userTeam.setUser(user);
        userTeam.setTeam(team);
        userTeam.setRole(role);

        return userTeamRepository.save(userTeam);
    }

    @DeleteMapping("/teams/{teamId}/members/{userId}")
    @Transactional
    public void removeMemberFromTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        UserTeam membership = team.getUserTeams()
                .stream()
                .filter(ut -> ut.getUser()
                        .getId()
                        .equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a member of this team"));

        // Both Team.userTeams and UserAccount.teams cascade with orphanRemoval and
        // are eagerly loaded. The join row is only deleted if it is orphaned on
        // BOTH sides; leaving it referenced by the user's collection makes
        // Hibernate re-persist it at flush, so we detach it from both parents.
        team.getUserTeams()
                .remove(membership);
        membership.getUser()
                .getTeams()
                .remove(membership);
    }

    @DeleteMapping("/teams/{teamId}")
    @Transactional
    public void deleteTeam(@PathVariable Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        teamRepository.delete(team);
    }
}