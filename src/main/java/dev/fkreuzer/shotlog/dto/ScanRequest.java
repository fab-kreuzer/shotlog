package dev.fkreuzer.shotlog.dto;

import dev.fkreuzer.shotlog.domain.datatypes.SessionType;

public record ScanRequest(String url, SessionType sessionType, Long seasonId, Long enemyId, Long teamId,
                          String title, Long overwriteSessionId) {
}
