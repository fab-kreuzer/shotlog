package dev.fkreuzer.shotlog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "series", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "series_number"}))
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    private int seriesNumber;

    @OneToMany(
            mappedBy = "series",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("shotNumber ASC")
    private List<Shot> shots = new ArrayList<>();


}
