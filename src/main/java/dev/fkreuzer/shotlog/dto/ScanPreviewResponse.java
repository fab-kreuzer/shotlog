package dev.fkreuzer.shotlog.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ScanPreviewResponse(String uuid, LocalDate date, LocalTime time, int shotCount, double total,
                                  boolean alreadyImported, List<ExistingSession> existingSessions) {

    public record ExistingSession(Long id, String title, String sessionType, String formattedShotSum) {
    }
}
