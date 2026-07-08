package dev.fkreuzer.shotlog.domain;

import java.io.Serializable;
import java.util.Objects;

public class UserTeamId implements Serializable {
    private Long user;
    private Long team;

    // Equals and HashCode are mandatory for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTeamId that = (UserTeamId) o;
        return Objects.equals(user, that.user) && Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, team);
    }
}
