package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.*;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.service.IcsImportService;
import dev.fkreuzer.shotlog.service.SessionService;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiSessionController extends DefaultShotLogController {

    private final SessionService sessionService;
    private final ShootingPlaceService shootingPlaceService;
    private final IcsImportService icsImportService;
    private final SeasonRepository seasonRepository;
    private final MessageSource messageSource;

    public ApiSessionController(SessionService sessionService, ShootingPlaceService shootingPlaceService,
                                IcsImportService icsImportService, SeasonRepository seasonRepository,
                                MessageSource messageSource) {
        this.sessionService = sessionService;
        this.shootingPlaceService = shootingPlaceService;
        this.icsImportService = icsImportService;
        this.seasonRepository = seasonRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> getAllSessions() {
        return ResponseEntity.ok(sessionService.findAllByUser(getCurrentUser()));
    }

    @GetMapping("/sessions/by-type")
    public ResponseEntity<List<Session>> getSessionsByType(@RequestParam String type) {
        SessionType sessionType = SessionType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(sessionService.findAllByUserAndType(getCurrentUser(), sessionType));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<Session> getSession(@PathVariable Long id) {
        return sessionService.findByIdAndUser(id, getCurrentUser())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound()
                        .build());
    }

    @PostMapping("/sessions")
    public ResponseEntity<Session> createSession(@RequestBody Map<String, Object> body) {
        Session session = mapToSession(body);
        session.setUser(getCurrentUser());
        Session saved = sessionService.save(session);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Session session = mapToSession(body);
        session.setId(id);
        session.setUser(getCurrentUser());
        Session saved = sessionService.save(session);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteByIdAndUser(id, getCurrentUser());
        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/sessions/import")
    public ResponseEntity<?> importSessions(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("session.importFileEmpty")));
        }

        try {
            int imported = icsImportService.importFromIcs(file.getInputStream(), getCurrentUser());
            return ResponseEntity.ok(Map.of("imported", imported));
        } catch (IOException e) {
            return ResponseEntity.unprocessableEntity()
                    .body(ApiResponse.error(msg("session.importReadError")));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Session mapToSession(Map<String, Object> body) {
        Session session = new Session();
        session.setSessionDate(LocalDate.parse((String) body.get("sessionDate")));
        session.setSessionTime(LocalTime.parse((String) body.get("sessionTime")));
        session.setTitle((String) body.get("title"));
        session.setSessionType(SessionType.valueOf((String) body.get("sessionType")));
        session.setDecimalScoring(Boolean.TRUE.equals(body.get("decimalScoring")));
        session.setHome(Boolean.TRUE.equals(body.get("home")));

        // Resolve enemy (ShootingPlace) by ID
        Object enemyId = body.get("enemyId");
        if (enemyId != null) {
            Long placeId = Long.valueOf(enemyId.toString());
            ShootingPlace place = shootingPlaceService.findById(placeId);
            session.setEnemy(place);
        }

        // Resolve season by ID
        Object seasonId = body.get("seasonId");
        if (seasonId != null) {
            Long id = Long.valueOf(seasonId.toString());
            Season season = seasonRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Season not found: " + id));
            session.setSeason(season);
        }

        // Map series
        List<Map<String, Object>> seriesList = (List<Map<String, Object>>) body.get("series");
        if (seriesList != null) {
            List<Series> seriesEntities = new ArrayList<>();
            for (Map<String, Object> seriesData : seriesList) {
                Series series = new Series();
                series.setSeriesNumber(((Number) seriesData.get("seriesNumber")).intValue());
                series.setTestShot(Boolean.TRUE.equals(seriesData.get("testShot")));
                series.setSession(session);

                List<Map<String, Object>> shotsList = (List<Map<String, Object>>) seriesData.get("shots");
                if (shotsList != null) {
                    List<Shot> shotEntities = new ArrayList<>();
                    for (Map<String, Object> shotData : shotsList) {
                        Shot shot = new Shot();
                        shot.setShotNumber(((Number) shotData.get("shotNumber")).intValue());
                        Object value = shotData.get("value");
                        if (value != null) {
                            shot.setValue(new BigDecimal(value.toString()));
                        }
                        shot.setSeries(series);
                        shotEntities.add(shot);
                    }
                    series.setShots(shotEntities);
                }
                seriesEntities.add(series);
            }
            session.setSeries(seriesEntities);
        } else {
            session.setSeries(new ArrayList<>());
        }

        return session;
    }
}