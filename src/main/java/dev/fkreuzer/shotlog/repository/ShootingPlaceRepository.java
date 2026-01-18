package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShootingPlaceRepository extends JpaRepository<ShootingPlace, Long> {
    List<ShootingPlace> findAll();
}
