package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.Team;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.domain.UserTeamId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeamId> {

    void deleteByUserAndTeam(UserAccount user, Team team);
}
