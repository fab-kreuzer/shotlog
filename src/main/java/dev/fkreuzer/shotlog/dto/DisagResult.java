package dev.fkreuzer.shotlog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Decoded result as returned by the n8n webhook. Shot values are ring x 10. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DisagResult(String id, Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String uuid, String startTime, List<Shot> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Shot(int value) {
    }
}
