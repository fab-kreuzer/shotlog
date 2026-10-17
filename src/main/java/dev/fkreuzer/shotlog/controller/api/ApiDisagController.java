package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.dto.DisagResult;
import dev.fkreuzer.shotlog.dto.ScanPreviewResponse;
import dev.fkreuzer.shotlog.dto.ScanRequest;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.service.DisagQrService;
import dev.fkreuzer.shotlog.service.SessionService;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions/scan")
@PreAuthorize("hasAuthority('scan_result')")
public class ApiDisagController extends DefaultShotLogController {

    private final DisagQrService disagQrService;
    private final SessionService sessionService;
    private final ShootingPlaceService shootingPlaceService;
    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final MessageSource messageSource;

    public ApiDisagController(DisagQrService disagQrService, SessionService sessionService,
                              ShootingPlaceService shootingPlaceService, SeasonRepository seasonRepository,
                              TeamRepository teamRepository, MessageSource messageSource) {
        this.disagQrService = disagQrService;
        this.sessionService = sessionService;
        this.shootingPlaceService = shootingPlaceService;
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ApiResponse> error(HttpStatus status, String key) {
        return ResponseEntity.status(status).body(ApiResponse.error(msg(key)));
    }

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@RequestBody Map<String, String> body) {
        try {
            DisagResult result = disagQrService.decode(body.get("url"));
            Session scanned = disagQrService.toSession(result);
            List<ScanPreviewResponse.ExistingSession> existing = sessionService
                    .findAllByUserAndDate(getCurrentUser(), scanned.getSessionDate()).stream()
                    .map(s -> new ScanPreviewResponse.ExistingSession(s.getId(), s.getTitle(),
                            s.getSessionType().name(), s.getFormattedShotSum()))
                    .toList();
            boolean imported = sessionService.existsByUserAndExternalId(getCurrentUser(), scanned.getExternalId());
            return ResponseEntity.ok(new ScanPreviewResponse(scanned.getExternalId(), scanned.getSessionDate(),
                    scanned.getSessionTime(), result.data().results().size(), scanned.getShotSum(), imported, existing));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> scan(@RequestBody ScanRequest request) {
        try {
            Session session = disagQrService.toSession(disagQrService.decode(request.url()));

            if (request.overwriteSessionId() == null
                    && sessionService.existsByUserAndExternalId(getCurrentUser(), session.getExternalId())) {
                return error(HttpStatus.CONFLICT, "scan.duplicate");
            }
            if (request.enemyId() == null) {
                return error(HttpStatus.BAD_REQUEST, "scan.noPlace");
            }
            if (request.seasonId() == null) {
                return error(HttpStatus.BAD_REQUEST, "scan.noSeason");
            }
            ShootingPlace place = shootingPlaceService.findById(request.enemyId());
            Season season = seasonRepository.findById(request.seasonId()).orElse(null);
            if (season == null) {
                return error(HttpStatus.BAD_REQUEST, "scan.noSeason");
            }
            Team team = request.teamId() == null ? null : teamRepository.findById(request.teamId()).orElse(null);

            session.setUser(getCurrentUser());
            session.setSessionType(request.sessionType());
            session.setEnemy(place);
            session.setSeason(season);
            session.setTeam(team);
            session.setTitle(request.title());

            if (request.overwriteSessionId() != null) {
                Session existing = sessionService.findByIdAndUser(request.overwriteSessionId(), getCurrentUser())
                        .orElse(null);
                if (existing == null) {
                    return error(HttpStatus.NOT_FOUND, "scan.sessionNotFound");
                }
                session.setId(existing.getId());
                session.setHome(existing.isHome());
            }
            return ResponseEntity.ok(sessionService.save(session));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return error(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}
