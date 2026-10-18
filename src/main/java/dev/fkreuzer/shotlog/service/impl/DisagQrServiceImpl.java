package dev.fkreuzer.shotlog.service.impl;

import dev.fkreuzer.shotlog.config.DisagProperties;
import dev.fkreuzer.shotlog.domain.Series;
import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.Shot;
import dev.fkreuzer.shotlog.dto.DisagResult;
import dev.fkreuzer.shotlog.service.DisagQrService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DisagQrServiceImpl implements DisagQrService {

    static final String QR_PREFIX = "https://qr.shotsapp.de/";
    static final int SERIES_SIZE = 10;
    static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private final RestClient restClient;
    private final DisagProperties properties;

    public DisagQrServiceImpl(RestClient disagRestClient, DisagProperties properties) {
        this.restClient = disagRestClient;
        this.properties = properties;
    }

    @Override
    public DisagResult decode(String qrUrl) {
        if (qrUrl == null || !qrUrl.trim().startsWith(QR_PREFIX)) {
            throw new IllegalArgumentException("scan.invalidLink");
        }
        String link = qrUrl.trim();
        RestClientException last = null;
        // Disag occasionally answers 500 for valid codes; one retry resolves it (see docs).
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                DisagResult result = restClient.post()
                        .uri(properties.webhookUrl())
                        .header("X-Api-Key", properties.webhookSecret())
                        .body(Map.of("url", link))
                        .retrieve()
                        .body(DisagResult.class);
                if (result == null || result.data() == null || result.data().results() == null) {
                    throw new IllegalStateException("scan.upstreamError");
                }
                return result;
            } catch (RestClientException e) {
                last = e;
            }
        }
        throw new IllegalStateException("scan.upstreamError", last);
    }

    @Override
    public Session toSession(DisagResult result) {
        LocalDateTime local = LocalDateTime.ofInstant(Instant.parse(result.data().startTime()), ZONE);
        Session session = new Session();
        session.setSessionDate(local.toLocalDate());
        session.setSessionTime(local.toLocalTime().withNano(0));
        session.setDecimalScoring(true);
        session.setExternalId(result.id() != null ? result.id() : result.data().uuid());

        session.setDiskType(diskType(result.data()));

        List<DisagResult.Shot> shots = result.data().results().stream().filter(DisagResult.Shot::usable).toList();
        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < shots.size(); i += SERIES_SIZE) {
            Series series = new Series();
            series.setSeriesNumber(i / SERIES_SIZE + 1);
            series.setSession(session);
            List<Shot> shotEntities = new ArrayList<>();
            for (int j = i; j < Math.min(i + SERIES_SIZE, shots.size()); j++) {
                Shot shot = new Shot();
                shot.setShotNumber(j - i + 1);
                shot.setValue(BigDecimal.valueOf(shots.get(j).value(), 1));
                shot.setX(shots.get(j).x());
                shot.setY(shots.get(j).y());
                shot.setSecondsSincePrevious(shots.get(j).offset());
                shot.setSeries(series);
                shotEntities.add(shot);
            }
            series.setShots(shotEntities);
            seriesList.add(series);
        }
        session.setSeries(seriesList);
        return session;
    }

    /** Disk type from {@code meta}, else from the U+001F separated {@code startTimeRaw}; defaults to {@code lg}. */
    private static String diskType(DisagResult.Data data) {
        if (data.meta() != null && data.meta().diskType() != null && !data.meta().diskType().isBlank()) {
            return data.meta().diskType().trim().toLowerCase();
        }
        if (data.startTimeRaw() != null) {
            String[] parts = data.startTimeRaw().split("\u001f");
            if (parts.length > 4 && !parts[3].isBlank()) {
                return parts[3].trim().toLowerCase();
            }
        }
        return "lg";
    }
}
