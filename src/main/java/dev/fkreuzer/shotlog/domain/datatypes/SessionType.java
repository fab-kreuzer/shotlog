package dev.fkreuzer.shotlog.domain.datatypes;

import lombok.Getter;

@Getter
public enum SessionType {
    TRAINING("Training"),
    COMPETITION("Wettkampf");

    private final String type;

    SessionType(String type) {
        this.type = type;
    }

    public String toUrlFormat() {
        return this.name().toLowerCase();
    }

}
