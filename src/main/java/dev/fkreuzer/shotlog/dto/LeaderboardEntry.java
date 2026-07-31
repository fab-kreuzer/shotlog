package dev.fkreuzer.shotlog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardEntry {
    private Long userId;
    private String displayName;
    private String username;
    private int rank;
    private long sessionCount;
    private double totalShotSum;
    private double averagePerSession;
    private double bestSessionShotSum;
    private double gapToLeader;
    private String trend;
    private boolean currentUser;
}
