package dev.fkreuzer.shotlog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Decoded result as returned by the n8n webhook. Shot values are ring x 10. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DisagResult(String id, Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String uuid, String startTime, String startTimeRaw, Meta meta, List<Shot> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(String diskType) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Shot(int value, Integer valid, Integer flag, Integer x, Integer y, Integer offset) {

        /** The app only draws and counts shots with flag 2 and valid 2. */
        public boolean usable() {
            return Integer.valueOf(2).equals(flag) && Integer.valueOf(2).equals(valid);
        }
    }
}
