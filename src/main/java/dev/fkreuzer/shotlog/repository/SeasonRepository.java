package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findByActiveTrue();
}
