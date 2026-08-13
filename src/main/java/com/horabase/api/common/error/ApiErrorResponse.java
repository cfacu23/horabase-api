package com.horabase.api.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {

    public static ApiErrorResponse of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    public static ApiErrorResponse validation(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> errors
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                errors
        );
    }
}
