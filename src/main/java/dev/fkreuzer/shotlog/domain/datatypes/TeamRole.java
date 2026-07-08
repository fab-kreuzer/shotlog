package dev.fkreuzer.shotlog.domain.datatypes;

import lombok.Getter;

@Getter
public enum TeamRole {
    MEMBER("Mitglied"),
    LEADER("Leiter");

    private final String type;

    TeamRole(String type) {
        this.type = type;
    }

}
