package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiLocationController {

    private final ShootingPlaceRepository shootingPlaceRepository;

    public ApiLocationController(ShootingPlaceRepository shootingPlaceRepository) {
        this.shootingPlaceRepository = shootingPlaceRepository;
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
                    .body(ApiResponse.error("Clubname ist erforderlich"));
        }
        if (location == null || location.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ort ist erforderlich"));
        }

        ShootingPlace place = new ShootingPlace();
        place.setClub(club.trim());
        place.setLocation(location.trim());
        shootingPlaceRepository.save(place);

        return ResponseEntity.ok()
                .body(ApiResponse.success("Der Club \"" + place.getClub() + "\" wurde erstellt."));
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<ApiResponse> updateLocation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<ShootingPlace> existing = shootingPlaceRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Der Club wurde nicht gefunden."));
        }

        ShootingPlace place = existing.get();

        if (body.containsKey("club")) {
            String club = body.get("club");
            if (club == null || club.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Clubname ist erforderlich"));
            }
            place.setClub(club.trim());
        }

        if (body.containsKey("location")) {
            String location = body.get("location");
            if (location == null || location.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Ort ist erforderlich"));
            }
            place.setLocation(location.trim());
        }

        shootingPlaceRepository.save(place);
        return ResponseEntity.ok()
                .body(ApiResponse.success("Der Club \"" + place.getClub() + "\" wurde aktualisiert."));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<ApiResponse> deleteLocation(@PathVariable Long id) {
        Optional<ShootingPlace> location = shootingPlaceRepository.findById(id);
        if (location.isEmpty()) {
            return ResponseEntity.notFound()
                    .build();
        }
        shootingPlaceRepository.deleteById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success("Der Club " + location.get()
                        .getClub() + " wurde gelöscht."));
    }
}
