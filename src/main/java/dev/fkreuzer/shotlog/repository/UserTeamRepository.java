package dev.fkreuzer.shotlog.repository;

import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.UserTeam;
import dev.fkreuzer.shotlog.domain.UserTeamId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTeamRepository extends JpaRepository<UserTeam, UserTeamId> {
    List<UserTeam> findUserTeamsByUser(UserAccount user);
}