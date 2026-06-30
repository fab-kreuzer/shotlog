package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiSeasonController {

    private final SeasonRepository seasonRepository;

    public ApiSeasonController(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @GetMapping("/seasons")
    public List<Season> getSeasons() {
        return seasonRepository.findAll();
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
                .body(ApiResponse.success("Die aktive Saison wurde auf " + target.getDescription() + " gesetzt!"));
    }
}
