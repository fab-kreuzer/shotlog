package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.dto.LeaderboardEntry;

import java.util.List;

public interface LeaderboardService {

    List<LeaderboardEntry> buildLeaderboard(Team team);

}
