package dev.fkreuzer.shotlog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.fkreuzer.shotlog.domain.datatypes.TeamRole;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private TeamRole role = TeamRole.MEMBER;
}

