package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.ShootingPlace;

import java.util.List;

public interface ShootingPlaceService {
    List<ShootingPlace> findAll();
    ShootingPlace findById(Long id);
}
