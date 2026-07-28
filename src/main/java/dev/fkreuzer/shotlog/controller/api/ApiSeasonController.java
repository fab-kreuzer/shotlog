package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.repository.TeamRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiSeasonController {

    private final SeasonRepository seasonRepository;
    private final TeamRepository teamRepository;
    private final SessionRepository sessionRepository;
    private final MessageSource messageSource;

    public ApiSeasonController(SeasonRepository seasonRepository, TeamRepository teamRepository, SessionRepository sessionRepository, MessageSource messageSource) {
        this.seasonRepository = seasonRepository;
        this.teamRepository = teamRepository;
        this.sessionRepository = sessionRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping("/seasons")
    public List<Season> getSeasons() {
        return seasonRepository.findAll();
    }

    @PostMapping("/seasons")
    @PreAuthorize("hasAuthority('create_season')")
    public ResponseEntity<ApiResponse> createSeason(@RequestBody Map<String, Object> request) {
        String description = description(request);
        if (description == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(msg("season.nameRequired")));
        }
        if (seasonRepository.findByDescription(description).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(msg("season.duplicate", description)));
        }

        Season season = new Season(description);
        seasonRepository.save(season);
        return ResponseEntity.ok(ApiResponse.success(msg("season.created", description)));
    }

    @PutMapping("/seasons/{id}")
    @PreAuthorize("hasAuthority('edit_season')")
    public ResponseEntity<ApiResponse> updateSeason(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Season season = seasonRepository.findById(id).orElse(null);
        if (season == null) {
            return ResponseEntity.notFound().build();
        }
        String description = description(request);
        if (description == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(msg("season.nameRequired")));
        }
        Optional<Season> existing = seasonRepository.findByDescription(description);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(msg("season.duplicate", description)));
        }

        season.setDescription(description);
        seasonRepository.save(season);
        return ResponseEntity.ok(ApiResponse.success(msg("season.updated", description)));
    }

    @DeleteMapping("/seasons/{id}")
    @PreAuthorize("hasAuthority('delete_season')")
    public ResponseEntity<ApiResponse> deleteSeason(@PathVariable Long id) {
        Season season = seasonRepository.findById(id).orElse(null);
        if (season == null) {
            return ResponseEntity.notFound().build();
        }
        if (season.isActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(msg("season.deleteActive")));
        }
        long teamCount = teamRepository.countBySeason(season);
        long sessionCount = sessionRepository.countBySeason(season);
        if (teamCount > 0 || sessionCount > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(msg("season.deleteInUse", season.getDescription(), teamCount, sessionCount)));
        }

        seasonRepository.delete(season);
        return ResponseEntity.ok(ApiResponse.success(msg("season.deleted", season.getDescription())));
    }

    // Extracts a trimmed, non-empty description from the request body, or null.
    private String description(Map<String, Object> request) {
        Object raw = request.get("description");
        if (raw == null) {
            return null;
        }
        String trimmed = raw.toString().trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @PutMapping("/seasons/{id}/active")
    @Transactional
    public ResponseEntity<ApiResponse> setActiveSeason(@PathVariable Long id) {
        Season target = seasonRepository.findById(id)
                .orElse(null);
        if (target == null) {
            return ResponseEntity.notFound()
                    .build();
        }
        // Deactivate the currently active season(s) first so the unique
        // "one active season" index is never violated mid-transaction.
        for (Season season : seasonRepository.findAll()) {
            if (season.isActive() && !season.getId()
                    .equals(id)) {
                season.setActive(false);
                seasonRepository.saveAndFlush(season);
            }
        }
        target.setActive(true);
        seasonRepository.saveAndFlush(target);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("season.activeSet", target.getDescription())));
    }
}
