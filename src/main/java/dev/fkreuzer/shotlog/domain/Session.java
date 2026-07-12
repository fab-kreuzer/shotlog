package dev.fkreuzer.shotlog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Getter
@Setter
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    private LocalDate sessionDate;
    private LocalTime sessionTime;

    @ManyToOne(optional = false)
    private ShootingPlace enemy;

    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    private boolean decimalScoring;
    private boolean isHome;

    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id")
    private Season season;

    // Optional: only competition sessions are tied to a team.
    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties("userTeams")
    private Team team;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seriesNumber ASC")
    private List<Series> series = new ArrayList<>();

    double getShotSum() {
        return series.stream()
                .map(Series::calculateShotSum)
                .reduce(0.0, Double::sum);
    }

    double getShotSumOfTestShots() {
        return series.stream()
                .map(Series::calculateShotSumForTestShots)
                .reduce(0.0, Double::sum);
    }

    public String getFormattedShotSum() {
        double sum = getShotSum();
        return decimalScoring ? String.format("%.1f", sum) : String.format("%.0f", sum);
    }

    public String getFormattedShotSumOfTestShots() {
        double sum = getShotSumOfTestShots();
        return decimalScoring ? String.format("%.1f", sum) : String.format("%.0f", sum);
    }

    public String getTranslatedLocation() {
        return this.getEnemy().getClub();
    }

    public String getFormattedType() {
        return sessionType.getType();
    }
}