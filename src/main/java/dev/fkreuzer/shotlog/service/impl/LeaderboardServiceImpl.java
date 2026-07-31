package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.dto.LeaderboardEntry;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.service.LeaderboardService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final SessionRepository sessionRepository;

    public LeaderboardServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<LeaderboardEntry> buildLeaderboard(Team team) {
        Map<Long, List<Session>> sessionsByUser = sessionRepository.findAllByTeam(team)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getUser()
                        .getId()));

        List<LeaderboardEntry> entries = team.getUserTeams()
                .stream()
                .map(UserTeam::getUser)
                .map(user -> buildEntry(user, sessionsByUser.getOrDefault(user.getId(), List.of())))
                .sorted(Comparator.comparingDouble(LeaderboardEntry::getTotalShotSum)
                        .reversed()
                        .thenComparing(LeaderboardEntry::getDisplayName, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        double leaderTotal = entries.isEmpty() ? 0 : entries.get(0)
                .getTotalShotSum();
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            entry.setRank(i + 1);
            entry.setGapToLeader(round1(leaderTotal - entry.getTotalShotSum()));
        }

        return entries;
    }

    private LeaderboardEntry buildEntry(UserAccount user, List<Session> sessions) {
        List<Session> chronological = sessions.stream()
                .sorted(Comparator.comparing(Session::getSessionDate)
                        .thenComparing(Session::getSessionTime))
                .toList();

        int count = chronological.size();
        double total = chronological.stream()
                .mapToDouble(Session::getShotSum)
                .sum();
        double average = count == 0 ? 0 : total / count;
        double best = chronological.stream()
                .mapToDouble(Session::getShotSum)
                .max()
                .orElse(0);

        // "Form": is the member's most recent session above or below their own
        // average for this team? A cheap, motivating signal without needing a
        // longer trend window.
        String trend = "FLAT";
        if (count > 0) {
            double last = chronological.get(count - 1)
                    .getShotSum();
            if (last > average) {
                trend = "UP";
            } else if (last < average) {
                trend = "DOWN";
            }
        }

        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setUserId(user.getId());
        entry.setDisplayName(user.getDisplayName() != null && !user.getDisplayName()
                .isBlank() ? user.getDisplayName() : user.getUsername());
        entry.setUsername(user.getUsername());
        entry.setSessionCount(count);
        entry.setTotalShotSum(round1(total));
        entry.setAveragePerSession(round1(average));
        entry.setBestSessionShotSum(round1(best));
        entry.setTrend(trend);
        return entry;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
