package org.nlh4j.socialscheduler.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT Authentication Filter for the API Gateway.
 * Intercepts incoming HTTP requests, extracts the Bearer token, validates it using JwtDecoder,
 * maps roles to authorities with the RO_ROLE_ prefix, and establishes the SecurityContext.
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // =========================================================================
    // GLOBAL CONSTANTS DECLARATION (Top-of-Class Constants Law)
    // =========================================================================
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ERROR_CODE_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_INVALID = "TOKEN_INVALID";
    public static final String SYSTEM_MODULE_NAME = "API-GATEWAY-AUTH";
    public static final String RESPONSE_CHARSET = "UTF-8";
    public static final String MASK_REPLACEMENT = "******";
    public static final int MASK_VISIBLE_LENGTH = 6;

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Constructor injecting the required security components.
     *
     * @param jwtDecoder                 the decoder to validate and parse JWTs
     * @param jwtAuthenticationConverter the converter to map JWT claims to GrantedAuthorities
     */
    public JwtAuthFilter(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /**
     * Filters each incoming request to perform JWT authentication.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // [PROCESS] Log entry point of the transaction filter
        log.debug("[PROCESS] Entering JwtAuthFilter for URI: {}", request.getRequestURI());

        // Extract the Authorization header from the incoming request
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Check if the Authorization header is present and starts with the Bearer prefix
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("[PROCESS] No Bearer token found in request headers. Proceeding with filter chain.");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the raw token string from the header
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Programmatically mask the token to prevent sensitive data leakage in logs (Sensitive Data Masking Law)
        String maskedToken = maskSensitiveToken(token);
        log.info("[PROCESS] Processing authentication scan for Token: {}", maskedToken);

        try {
            // Decode and validate the token using the configured JwtDecoder
            Jwt jwt = this.jwtDecoder.decode(token);

            // Convert the decoded JWT into a Spring Security Authentication token
            JwtAuthenticationToken authentication = (JwtAuthenticationToken) this.jwtAuthenticationConverter.convert(jwt);

            if (authentication != null) {
                // Set the authenticated token into the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[PROCESS] Successfully authenticated user: {} with authorities: {}", 
                        authentication.getName(), authentication.getAuthorities());
            }

            // Proceed with the downstream filter chain execution
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // [ARC-005] Handle JWT validation failures securely without leaking internal details (OWASP A09)
            handleJwtException(response, e);
        } finally {
            // [PROCESS] Log exit point of the transaction filter
            log.debug("[PROCESS] Exiting JwtAuthFilter for URI: {}", request.getRequestURI());
        }
    }

    /**
     * Programmatically masks the JWT token to prevent credential exposure in logs.
     *
     * @param token the raw JWT token
     * @return the masked token string
     */
    private String maskSensitiveToken(String token) {
        if (token == null || token.length() <= (MASK_VISIBLE_LENGTH * 2)) {
            return MASK_REPLACEMENT;
        }
        return token.substring(0, MASK_VISIBLE_LENGTH) 
                + MASK_REPLACEMENT 
                + token.substring(token.length() - MASK_VISIBLE_LENGTH);
    }

    /**
     * Handles JWT exceptions by writing a standardized JSON error response.
     * Ensures compliance with OWASP Top 10 (A05: Security Misconfiguration / A09: Security Logging and Monitoring).
     *
     * @param response the HTTP response object
     * @param e        the caught JwtException
     * @throws IOException if an input or output exception occurs
     */
    private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
        // Clear the security context to prevent unauthorized access
        SecurityContextHolder.clearContext();

        // Determine if the token has expired or is structurally invalid
        String errorCode = ERROR_CODE_INVALID;
        String userMessage = "Yêu cầu xác thực không hợp lệ. Vui lòng đăng nhập lại.";

        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("expired")) {
            errorCode = ERROR_CODE_EXPIRED;
            userMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
        }

        // [ARC-005] Log the exception with the required 3 context keys: Subsystem, Raw Message, and Tag ID
        log.error("[CRITICAL FAIL] [ARC-005] JWT processing failed in subsystem: {}. Raw error: {}", 
                SYSTEM_MODULE_NAME, e.getMessage());

        // Set response headers and status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(RESPONSE_CHARSET);

        // Construct a clean, standardized JSON payload without leaking internal stack traces (OWASP A09)
        String jsonResponse = String.format(
                "{\"errorCode\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                errorCode,
                userMessage,
                Instant.now().toString()
        );

        // Write the response payload to the client
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}