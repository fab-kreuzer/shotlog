package dev.fkreuzer.shotlog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "team")
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Teams are scoped to a season; each season has its own set of teams.
    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id")
    private Season season;

    // Link back to the junction entity
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserTeam> userTeams;
}