package dev.fkreuzer.shotlog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_team")
@IdClass(UserTeamId.class)
@Getter
@Setter
public class UserTeam {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Id
    @ManyToOne
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;

    @Column(name = "role")
    private String role = "MEMBER";
}

