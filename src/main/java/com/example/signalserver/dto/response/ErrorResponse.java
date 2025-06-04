package com.example.signalserver.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String error;
    private String message;
    private int status;
    private String path;
    private String method;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String traceId;
    private List<ValidationError> validationErrors;
    private Map<String, Object> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    // Статические методы для создания стандартных ошибок
    public static ErrorResponse badRequest(String message) {
        return ErrorResponse.builder()
                .error("Bad Request")
                .message(message)
                .status(400)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse unauthorized(String message) {
        return ErrorResponse.builder()
                .error("Unauthorized")
                .message(message)
                .status(401)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse forbidden(String message) {
        return ErrorResponse.builder()
                .error("Forbidden")
                .message(message)
                .status(403)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse notFound(String message) {
        return ErrorResponse.builder()
                .error("Not Found")
                .message(message)
                .status(404)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse internalServerError(String message) {
        return ErrorResponse.builder()
                .error("Internal Server Error")
                .message(message)
                .status(500)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
