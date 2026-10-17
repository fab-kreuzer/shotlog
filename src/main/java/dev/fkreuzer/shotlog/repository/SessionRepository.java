package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserAndSessionTypeOrderBySessionDateAscSessionTimeAsc(UserAccount user, SessionType type);
    Optional<Session> findByIdAndUser(Long id, UserAccount user);
    void deleteByIdAndUser(Long id, UserAccount user);
    List<Session> findAllByUser(UserAccount user);
    List<Session> findAllByTeam(Team team);
    List<Session> findAllByUserAndSessionDate(UserAccount user, LocalDate sessionDate);
    boolean existsByUserAndExternalId(UserAccount user, String externalId);
    long countBySeason(Season season);

    long countByEnemyId(Long enemyId);
}
