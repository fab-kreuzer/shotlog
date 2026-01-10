package dev.fkreuzer.shotlog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "shot",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series_id", "shot_number"})
        }
)
@Getter
@Setter
public class Shot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "series_id")
    private Series series;

    private int shotNumber;

    @Column(precision = 4, scale = 1)
    private BigDecimal value;
}
