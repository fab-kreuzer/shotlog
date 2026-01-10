package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.Series;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Shot;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.service.SessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session save(Session session) {

        for (Series series : session.getSeries()) {
            series.setSession(session);
            for (Shot shot : series.getShots()) {
                shot.setSeries(series);
            }
        }

        return sessionRepository.save(session);
    }

    @Override
    public List<Session> findAllByUser(UserAccount user) {
        return sessionRepository.findAllByUserOrderBySessionDateAscSessionTimeAsc(user);
    }

    @Override
    public Optional<Session> findByIdAndUser(Long id, UserAccount user) {
        return sessionRepository.findByIdAndUser(id, user);
    }

    @Override
    public void deleteByIdAndUser(Long id, UserAccount user) {
        sessionRepository.deleteByIdAndUser(id, user);
    }
}
