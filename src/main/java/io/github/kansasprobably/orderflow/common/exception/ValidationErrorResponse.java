package io.github.kansasprobably.orderflow.common.exception;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String message,
        Map<String,String> errors
) {
}
