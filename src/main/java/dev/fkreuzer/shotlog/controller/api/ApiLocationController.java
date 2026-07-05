package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiLocationController {

    private final ShootingPlaceRepository shootingPlaceRepository;
    private final SessionRepository sessionRepository;
    private final UserAccountRepository userAccountRepository;
    private final MessageSource messageSource;

    public ApiLocationController(ShootingPlaceRepository shootingPlaceRepository,
                                 SessionRepository sessionRepository,
                                 UserAccountRepository userAccountRepository,
                                 MessageSource messageSource) {
        this.shootingPlaceRepository = shootingPlaceRepository;
        this.sessionRepository = sessionRepository;
        this.userAccountRepository = userAccountRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping("/locations")
    public ResponseEntity<List<ShootingPlace>> getLocations() {
        return ResponseEntity.ok(shootingPlaceRepository.findAll());

    }

    @PostMapping("/locations")
    public ResponseEntity<ApiResponse> createLocation(@RequestBody Map<String, String> body) {
        String club = body.get("club");
        String location = body.get("location");

        if (club == null || club.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("club.nameRequired")));
        }
        if (location == null || location.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("club.locationRequired")));
        }

        ShootingPlace place = new ShootingPlace();
        place.setClub(club.trim());
        place.setLocation(location.trim());
        shootingPlaceRepository.save(place);

        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("club.created", place.getClub())));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<ApiResponse> updateLocation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<ShootingPlace> existing = shootingPlaceRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("club.notFound")));
        }

        ShootingPlace place = existing.get();

        if (body.containsKey("club")) {
            String club = body.get("club");
            if (club == null || club.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("club.nameRequired")));
            }
            place.setClub(club.trim());
        }

        if (body.containsKey("location")) {
            String location = body.get("location");
            if (location == null || location.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("club.locationRequired")));
            }
            place.setLocation(location.trim());
        }

        shootingPlaceRepository.save(place);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("club.updated", place.getClub())));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<ApiResponse> deleteLocation(@PathVariable Long id) {
        Optional<ShootingPlace> location = shootingPlaceRepository.findById(id);
        if (location.isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }

        long sessionCount = sessionRepository.countByEnemyId(id);
        if (sessionCount > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("club.deleteInUseSessions", location.get()
                            .getClub(), sessionCount)));
        }

        long homeClubCount = userAccountRepository.countByHomeClubId(id);
        if (homeClubCount > 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("club.deleteInUseHomeClub", location.get()
                            .getClub(), homeClubCount)));
        }

        shootingPlaceRepository.deleteById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("club.deleted", location.get()
                        .getClub())));
    }
}
