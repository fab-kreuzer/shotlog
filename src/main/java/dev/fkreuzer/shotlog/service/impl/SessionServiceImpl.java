package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.domain.Series;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Shot;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.repository.SessionRepository;
import dev.fkreuzer.shotlog.service.SessionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // Check if this is an update (session has an ID) and the session exists in the database
        if (session.getId() != null) {
            Optional<Session> existingSessionOpt = sessionRepository.findById(session.getId());

            if (existingSessionOpt.isPresent()) {
                Session existingSession = existingSessionOpt.get();

                // Create a map of existing series by seriesNumber for quick lookup
                Map<Integer, Series> existingSeriesMap = new HashMap<>();
                for (Series existingSeries : existingSession.getSeries()) {
                    existingSeriesMap.put(existingSeries.getSeriesNumber(), existingSeries);
                }

                // Process each series in the updated session
                for (Series updatedSeries : session.getSeries()) {
                    // Check if this series already exists (by seriesNumber)
                    Series existingSeries = existingSeriesMap.get(updatedSeries.getSeriesNumber());

                    if (existingSeries != null) {
                        // Update existing series - preserve its ID
                        updatedSeries.setId(existingSeries.getId());

                        // Create a map of existing shots by shotNumber for quick lookup
                        Map<Integer, Shot> existingShotsMap = new HashMap<>();
                        for (Shot existingShot : existingSeries.getShots()) {
                            existingShotsMap.put(existingShot.getShotNumber(), existingShot);
                        }

                        // Process each shot in the updated series
                        if (updatedSeries.getShots() != null) {
                            for (Shot updatedShot : updatedSeries.getShots()) {
                                // Check if this shot already exists (by shotNumber)
                                Shot existingShot = existingShotsMap.get(updatedShot.getShotNumber());

                                if (existingShot != null) {
                                    // Update existing shot - preserve its ID
                                    updatedShot.setId(existingShot.getId());
                                }

                                // Set the series reference
                                updatedShot.setSeries(updatedSeries);
                            }
                        }
                    } else {
                        // This is a new series being added during an update
                        // Ensure all shots have their series reference set
                        if (updatedSeries.getShots() != null) {
                            for (Shot updatedShot : updatedSeries.getShots()) {
                                updatedShot.setSeries(updatedSeries);
                            }
                        }
                    }

                    // Set the session reference
                    updatedSeries.setSession(session);
                }
            }
        } else {
            // This is a new session, just set the references
            for (Series series : session.getSeries()) {
                series.setSession(session);
                // Ensure all shots have their series reference set
                if (series.getShots() != null) {
                    for (Shot shot : series.getShots()) {
                        shot.setSeries(series);
                    }
                }
            }
        }

        return sessionRepository.save(session);
    }

    @Override
    public List<Session> findAllByUserAndType(UserAccount user, SessionType type) {
        return sessionRepository.findAllByUserAndSessionTypeOrderBySessionDateAscSessionTimeAsc(user, type);
    }

    @Override
    public Optional<Session> findByIdAndUser(Long id, UserAccount user) {
        return sessionRepository.findByIdAndUser(id, user);
    }

    @Override
    public void deleteByIdAndUser(Long id, UserAccount user) {
        sessionRepository.deleteByIdAndUser(id, user);
    }

    @Override
    public List<Session> findAllByUser(UserAccount user) {
        return sessionRepository.findAllByUser(user);
    }
}
