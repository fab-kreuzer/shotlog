package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findAllBySeason(Season season);
    long countBySeason(Season season);
}
