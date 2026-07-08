package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
