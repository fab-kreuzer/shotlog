package dev.fkreuzer.shotlog.web;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard envelope for API responses that carry user-facing messages.
 * Serializes to {@code {"errors":[...],"warnings":[...],"successes":[...],"infos":[...]}}.
 * The frontend automatically displays each message with the matching notification type.
 */
@Getter
public class ApiResponse {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> successes = new ArrayList<>();
    private final List<String> infos = new ArrayList<>();

    public ApiResponse addError(String message) {
        errors.add(message);
        return this;
    }

    public ApiResponse addWarning(String message) {
        warnings.add(message);
        return this;
    }

    public ApiResponse addSuccess(String message) {
        successes.add(message);
        return this;
    }

    public ApiResponse addInfo(String message) {
        infos.add(message);
        return this;
    }

    public static ApiResponse error(String... messages) {
        ApiResponse response = new ApiResponse();
        response.errors.addAll(Arrays.asList(messages));
        return response;
    }

    public static ApiResponse warning(String... messages) {
        ApiResponse response = new ApiResponse();
        response.warnings.addAll(Arrays.asList(messages));
        return response;
    }

    public static ApiResponse success(String... messages) {
        ApiResponse response = new ApiResponse();
        response.successes.addAll(Arrays.asList(messages));
        return response;
    }

    public static ApiResponse info(String... messages) {
        ApiResponse response = new ApiResponse();
        response.infos.addAll(Arrays.asList(messages));
        return response;
    }
}
