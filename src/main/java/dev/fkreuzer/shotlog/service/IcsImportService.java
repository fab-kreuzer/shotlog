package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.Season;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.repository.SeasonRepository;
import dev.fkreuzer.shotlog.repository.ShootingPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an uploaded iCalendar (.ics) file and persists each VEVENT as a {@link Session}
 * for the importing user. The parser is intentionally dependency-free: it handles RFC 5545
 * line unfolding, property parameters and the common DTSTART value forms (local, UTC, all-day).
 */
@Service
public class IcsImportService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final SessionService sessionService;
    private final ShootingPlaceRepository shootingPlaceRepository;
    private final SeasonRepository seasonRepository;

    public IcsImportService(SessionService sessionService, ShootingPlaceRepository shootingPlaceRepository,
                            SeasonRepository seasonRepository) {
        this.sessionService = sessionService;
        this.shootingPlaceRepository = shootingPlaceRepository;
        this.seasonRepository = seasonRepository;
    }

    /**
     * @return the number of sessions successfully imported.
     */
    @Transactional
    public int importFromIcs(InputStream input, UserAccount user) throws IOException {
        Season activeSeason = seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Es ist keine aktive Saison gesetzt"));
        int imported = 0;
        for (Map<String, String> event : parseEvents(input)) {
            Session session = toSession(event, user);
            if (session != null) {
                session.setSeason(activeSeason);
                sessionService.save(session);
                imported++;
            }
        }
        return imported;
    }

    private List<Map<String, String>> parseEvents(InputStream input) throws IOException {
        List<Map<String, String>> events = new ArrayList<>();
        Map<String, String> current = null;

        for (String line : unfold(input)) {
            if ("BEGIN:VEVENT".equalsIgnoreCase(line)) {
                current = new HashMap<>();
            } else if ("END:VEVENT".equalsIgnoreCase(line)) {
                if (current != null) {
                    events.add(current);
                }
                current = null;
            } else if (current != null) {
                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String descriptor = line.substring(0, colon);
                String value = line.substring(colon + 1);

                int semi = descriptor.indexOf(';');
                String name = (semi < 0 ? descriptor : descriptor.substring(0, semi)).toUpperCase();
                String params = semi < 0 ? "" : descriptor.substring(semi + 1);

                current.put(name, value);
                if (!params.isEmpty()) {
                    current.put(name + ";PARAMS", params);
                }
            }
        }
        return events;
    }

    /**
     * Reads the stream and reverses RFC 5545 line folding: a line beginning with a space or
     * tab is a continuation of the previous one.
     */
    private List<String> unfold(InputStream input) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder current = new StringBuilder();
            boolean started = false;
            String raw;
            while ((raw = reader.readLine()) != null) {
                if (!raw.isEmpty() && (raw.charAt(0) == ' ' || raw.charAt(0) == '\t')) {
                    current.append(raw, 1, raw.length());
                } else {
                    if (started) {
                        lines.add(current.toString());
                    }
                    current.setLength(0);
                    current.append(raw);
                    started = true;
                }
            }
            if (started) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private Session toSession(Map<String, String> event, UserAccount user) {
        String dtStart = event.get("DTSTART");
        if (dtStart == null) {
            return null;
        }
        LocalDateTime start = parseDateTime(dtStart.trim(), event.getOrDefault("DTSTART;PARAMS", ""));
        if (start == null) {
            return null;
        }

        Session session = new Session();
        session.setUser(user);
        session.setSessionDate(start.toLocalDate());
        session.setSessionTime(start.toLocalTime());
        session.setSessionType(determineSessionType(event));
        session.setDecimalScoring(false);
        session.setTitle(unescape(event.get("SUMMARY")));
        session.setEnemy(resolvePlace(unescape(event.get("LOCATION"))));
        if (user.getHomeClub() == null) {
            session.setHome(false);
        } else {
            session.setHome(session.getEnemy()
                    .getId()
                    .equals(user.getHomeClub().getId()));
        }
        session.setSeries(new ArrayList<>());
        return session;
    }

    private LocalDateTime parseDateTime(String value, String params) {
        try {
            // All-day events: VALUE=DATE parameter or a bare 8-digit date.
            if (params.toUpperCase()
                    .contains("VALUE=DATE") || value.length() == 8) {
                return LocalDate.parse(value.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE)
                        .atStartOfDay();
            }

            boolean utc = value.endsWith("Z");
            if (utc) {
                value = value.substring(0, value.length() - 1);
            }
            LocalDateTime local = LocalDateTime.parse(value, DATE_TIME);
            if (utc) {
                // Convert the UTC instant to the server's local wall-clock time.
                return local.atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
            return local;
        } catch (Exception e) {
            return null;
        }
    }

    private SessionType determineSessionType(Map<String, String> event) {
        String haystack = (event.getOrDefault("SUMMARY", "") + " " + event.getOrDefault("DESCRIPTION", ""))
                .toLowerCase();
        if (haystack.contains("rwk")) {
            return SessionType.COMPETITION;
        }
        return SessionType.TRAINING;
    }

    /**
     * Maps an event LOCATION to a {@link ShootingPlace}: reuses an existing place whose club or
     * location matches, otherwise creates one. Falls back to a placeholder when no location is given,
     * since {@link Session#getEnemy()} is non-nullable.
     */
    private ShootingPlace resolvePlace(String location) {
        List<ShootingPlace> places = shootingPlaceRepository.findAll();

        if (location != null && !location.isBlank()) {
            String needle = location.trim();
            for (ShootingPlace place : places) {
                if (needle.equalsIgnoreCase(place.getClub()) || needle.equalsIgnoreCase(place.getLocation())) {
                    return place;
                }
            }
            ShootingPlace place = new ShootingPlace();
            place.setClub(needle);
            place.setLocation(needle);
            return shootingPlaceRepository.save(place);
        }

        if (!places.isEmpty()) {
            return places.get(0);
        }
        ShootingPlace place = new ShootingPlace();
        place.setClub("Unbekannt");
        place.setLocation("Unbekannt");
        return shootingPlaceRepository.save(place);
    }

    private String unescape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\n", "\n")
                .replace("\\N", "\n")
                .replace("\\,", ",")
                .replace("\\;", ";")
                .replace("\\\\", "\\");
    }
}