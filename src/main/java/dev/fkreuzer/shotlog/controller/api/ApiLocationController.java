package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiLocationController {

    private final ShootingPlaceService shootingPlaceService;

    public ApiLocationController(ShootingPlaceService shootingPlaceService) {
        this.shootingPlaceService = shootingPlaceService;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<ShootingPlace>> getLocations() {
        return ResponseEntity.ok(shootingPlaceService.findAll());

    }

}
