package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserAndSessionTypeOrderBySessionDateAscSessionTimeAsc(UserAccount user, SessionType type);
    Optional<Session> findByIdAndUser(Long id, UserAccount user);
    void deleteByIdAndUser(Long id, UserAccount user);
}
