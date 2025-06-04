package com.example.signalserver.security;

import com.example.signalserver.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        logger.error("Unauthorized access attempt: {} from IP: {} with User-Agent: {}",
                requestURI, clientIp, userAgent);
        logger.debug("Authentication exception details: ", authException);

        // Устанавливаем заголовки ответа
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        // Добавляем CORS заголовки если нужно
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        // Создаем детализированный ответ об ошибке
        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        // Отправляем ответ
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Создает структурированный ответ об ошибке
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request,
                                                    AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", getErrorMessage(authException));
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("method", request.getMethod());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        // Добавляем дополнительную информацию в зависимости от типа ошибки
        if (authException.getMessage() != null) {
            if (authException.getMessage().contains("JWT")) {
                errorResponse.put("errorType", "INVALID_TOKEN");
                errorResponse.put("hint", "Please provide a valid JWT token in Authorization header");
            } else if (authException.getMessage().contains("expired")) {
                errorResponse.put("errorType", "TOKEN_EXPIRED");
                errorResponse.put("hint", "Please refresh your token");
            } else {
                errorResponse.put("errorType", "AUTHENTICATION_REQUIRED");
                errorResponse.put("hint", "Please login to access this resource");
            }
        }

        return errorResponse;
    }

    /**
     * Получает безопасное сообщение об ошибке (без раскрытия внутренних деталей)
     */
    private String getErrorMessage(AuthenticationException authException) {
        String message = authException.getMessage();

        // Возвращаем более пользовательские сообщения
        if (message == null) {
            return "Authentication required";
        }

        if (message.contains("JWT signature does not match")) {
            return "Invalid authentication token";
        }

        if (message.contains("JWT expired")) {
            return "Authentication token has expired";
        }

        if (message.contains("JWT token is malformed")) {
            return "Malformed authentication token";
        }

        // Для остальных случаев возвращаем общее сообщение
        return "Authentication required";
    }

    /**
     * Получает реальный IP адрес клиента (учитывает прокси и load balancer)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Проверяем различные заголовки для получения реального IP
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "X-Originating-IP",
                "CF-Connecting-IP",
                "True-Client-IP"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For может содержать несколько IP через запятую
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
