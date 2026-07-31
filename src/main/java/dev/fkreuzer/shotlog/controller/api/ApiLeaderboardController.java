package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.dto.LeaderboardEntry;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.service.LeaderboardService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiLeaderboardController extends DefaultShotLogController {

    private final TeamRepository teamRepository;
    private final LeaderboardService leaderboardService;

    public ApiLeaderboardController(TeamRepository teamRepository, LeaderboardService leaderboardService) {
        this.teamRepository = teamRepository;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard/{teamId}")
    @PreAuthorize("hasAuthority('view_leaderboard')")
    public List<LeaderboardEntry> getLeaderboard(@PathVariable Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        UserAccount currentUser = getCurrentUser();
        boolean isMember = team.getUserTeams()
                .stream()
                .anyMatch(ut -> ut.getUser()
                        .getId()
                        .equals(currentUser.getId()));
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this team");
        }

        List<LeaderboardEntry> entries = leaderboardService.buildLeaderboard(team);
        entries.forEach(entry -> entry.setCurrentUser(entry.getUserId()
                .equals(currentUser.getId())));
        return entries;
    }
}
