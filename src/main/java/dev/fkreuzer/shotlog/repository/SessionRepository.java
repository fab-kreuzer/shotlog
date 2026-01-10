package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserOrderBySessionDateAscSessionTimeAsc(UserAccount user);
    Optional<Session> findByIdAndUser(Long id, UserAccount user);
    void deleteByIdAndUser(Long id, UserAccount user);
}
