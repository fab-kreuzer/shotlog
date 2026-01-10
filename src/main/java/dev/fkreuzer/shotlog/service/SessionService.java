package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.UserAccount;

import java.util.List;
import java.util.Optional;

public interface SessionService {

    Session save(Session session);
    List<Session> findAllByUser(UserAccount user);
    Optional<Session> findByIdAndUser(Long id, UserAccount user);
    void deleteByIdAndUser(Long id, UserAccount user);

}
