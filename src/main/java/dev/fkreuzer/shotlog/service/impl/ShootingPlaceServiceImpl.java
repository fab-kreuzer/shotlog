package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShootingPlaceServiceImpl implements ShootingPlaceService {
    private final ShootingPlaceRepository shootingPlaceRepository;

    public ShootingPlaceServiceImpl(ShootingPlaceRepository shootingPlaceRepository) {
        this.shootingPlaceRepository = shootingPlaceRepository;
    }

    @Override
    public List<ShootingPlace> findAll() {
        return shootingPlaceRepository.findAll();
    }

    @Override
    public ShootingPlace findById(Long id) {
        return shootingPlaceRepository.findById(id).orElse(new ShootingPlace());
    }
}
