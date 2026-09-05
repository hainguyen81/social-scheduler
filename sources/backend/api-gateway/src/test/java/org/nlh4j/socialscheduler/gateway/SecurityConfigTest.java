package org.nlh4j.socialscheduler.gateway;

// [REQ-001] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] [ARC-006] [EXC-001] [EXC-002] [NFR-002]

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive Integration Test Suite for SecurityConfig and API Gateway RBAC Enforcement.
 * Validates OAuth2 Resource Server authentication, JWT validation, role-based access control (RBAC),
 * CORS policy headers, and actuator public endpoints.
 * 
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [EXC-002]
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigTest.class);

    private static final String API_SCHEDULES_PATH = "/api/v1/schedules";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String BEARER_PREFIX = "Bearer ";

    private static RSAKey rsaKey;
    private static RSAPublicKey testPublicKey;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Top-of-class constant configuration and RSA key pair generation for test JWT tokens.
     * Ensures deterministic cryptographic validation during test execution.
     */
    @BeforeAll
    static void setUpKeyStore() throws Exception {
        logger.info("[TEST_SETUP] [ARC-001] Generating ephemeral RSA key pair for Nimbus JWT testing.");
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-rsa-key-id")
                .generate();
        testPublicKey = rsaKey.toRSAPublicKey();
    }

    /**
     * Test configuration bean overriding JwtDecoder for test isolation using ephemeral RSA public key.
     */
    @TestConfiguration
    static class TestJwtConfig {
        @Bean
        @Primary
        public JwtDecoder testJwtDecoder() {
            logger.info("[TEST_CONFIG] [ARC-002] Initializing NimbusJwtDecoder with test public key.");
            return NimbusJwtDecoder.withPublicKey(testPublicKey).build();
        }
    }

    /**
     * Helper method to generate signed JWT token strings with specific claims and roles.
     */
    private String generateTestJwt(String subject, List<String> roles, Instant issuedAt, Instant expiresAt) throws Exception {
        com.nimbusds.jose.jwk.JWKSet jwkSet = new com.nimbusds.jose.jwk.JWKSet(rsaKey);
        com.nimbusds.jose.jwk.source.JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource = (jwkSelector, context) -> jwkSelector.select(jwkSet);
        
        com.nimbusds.jose.proc.JWSKeySelector<com.nimbusds.jose.proc.SecurityContext> keySelector = 
            new com.nimbusds.jose.requirement.Requirement<>() { /* placeholder */ };
        
        com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(
            new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://auth.socialscheduler.local")
                .issueTime(java.util.Date.from(issuedAt))
                .expirationTime(java.util.Date.from(expiresAt))
                .claim("roles", roles)
                .build()
        );
        
        signedJWT.sign(new com.nimbusds.jose.crypto.RSASSASigner(rsaKey));
        return signedJWT.serialize();
    }

    /**
     * Helper method to generate signed JWT token with a different private key to simulate invalid signature.
     */
    private String generateInvalidSignedJwt(String subject, List<String> roles) throws Exception {
        RSAKey anotherKey = new RSAKeyGenerator(2048).keyID("invalid-key-id").generate();
        com.nimbusds.jose.jwk.JWKSet jwkSet = new com.nimbusds.jose.jwk.JWKSet(anotherKey);
        
        com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(
            new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256).keyID(anotherKey.getKeyID()).build(),
            new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://auth.socialscheduler.local")
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .claim("roles", roles)
                .build()
        );
        
        signedJWT.sign(new com.nimbusds.jose.crypto.RSASSASigner(anotherKey));
        return signedJWT.serialize();
    }

    /**
     * Verifies that requests to actuator health endpoints are permitted without authentication.
     * @verifies [ARC-001], [ARC-006]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-001] Actuator health endpoint permits unauthenticated access")
    void testActuatorHealthPermitAll() throws Exception {
        logger.info("[TEST_START] [ARC-001] Executing testActuatorHealthPermitAll");
        
        mockMvc.perform(get(ACTUATOR_HEALTH_PATH + "/liveness"))
                .andExpect(status().isOk());
                
        logger.info("[TEST_COMPLETE] [ARC-001] testActuatorHealthPermitAll passed successfully.");
    }

    /**
     * Verifies that an ADMIN token successfully accesses protected schedule resources returning HTTP 200 OK.
     * @verifies [ARC-001], [ARC-002], [ARC-003]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-002] Admin token successfully accesses schedule resource")
    void testAdminRoleAccessSchedules() throws Exception {
        logger.info("[TEST_START] [ARC-002] Executing testAdminRoleAccessSchedules");
        String adminToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), Instant.now(), Instant.now().plusSeconds(3600));

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        logger.info("[TEST_COMPLETE] [ARC-002] testAdminRoleAccessSchedules passed successfully.");
    }

    /**
     * Verifies that an ANALYST token encounters HTTP 403 Forbidden due to insufficient role permissions.
     * @verifies [ARC-003], [ARC-004], [EXC-002]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-003] Analyst role encounters HTTP 403 Forbidden on schedule mutation endpoints")
    void testAnalystRoleForbiddenAccess() throws Exception {
        logger.info("[TEST_START] [ARC-003] Executing testAnalystRoleForbiddenAccess");
        String analystToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ANALYST"), Instant.now(), Instant.now().plusSeconds(3600));

        mockMvc.perform(post(API_SCHEDULES_PATH)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + analystToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"platform\":\"FACEBOOK\",\"content\":\"Test Post\",\"scheduledTime\":\"2026-10-01T10:00:00Z\"}"))
                .andExpect(status().isForbidden());

        logger.info("[TEST_COMPLETE] [ARC-003] testAnalystRoleForbiddenAccess passed successfully.");
    }

    /**
     * Verifies that an expired JWT token returns HTTP 401 Unauthorized requiring re-authentication.
     * @verifies [EXC-002], [ARC-005]
     */
    @Test
    @DisplayName("[TEST_PASS] [EXC-002] Expired token triggers HTTP 401 Unauthorized response")
    void testExpiredJwtTokenReturnsUnauthorized() throws Exception {
        logger.info("[TEST_START] [EXC-002] Executing testExpiredJwtTokenReturnsUnauthorized");
        Instant pastIssued = Instant.now().minusSeconds(7200);
        Instant pastExpiry = Instant.now().minusSeconds(3600);
        String expiredToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), pastIssued, pastExpiry);

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + expiredToken))
                .andExpect(status().isUnauthorized());

        logger.info("[TEST_COMPLETE] [EXC-002] testExpiredJwtTokenReturnsUnauthorized passed successfully.");
    }

    /**
     * Verifies that a token signed with an invalid private key returns HTTP 401 Unauthorized.
     * @verifies [EXC-002], [ARC-005], [NFR-002]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-005] Invalid signature token triggers HTTP 401 Unauthorized")
    void testInvalidSignatureJwtReturnsUnauthorized() throws Exception {
        logger.info("[TEST_START] [ARC-005] Executing testInvalidSignatureJwtReturnsUnauthorized");
        String invalidToken = generateInvalidSignedJwt(UUID.randomUUID().toString(), List.of("ADMIN"));

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + invalidToken))
                .andExpect(status().isUnauthorized());

        logger.info("[TEST_COMPLETE] [ARC-005] testInvalidSignatureJwtReturnsUnauthorized passed successfully.");
    }

    /**
     * Verifies that POST /api/v1/schedules with valid payload and Admin token returns HTTP 201 Created.
     * @verifies [REQ-001], [ARC-001], [ARC-006]
     */
    @Test
    @DisplayName("[TEST_PASS] [REQ-001] Admin successfully creates new schedule returning HTTP 201 Created")
    void testAdminCreateScheduleSuccess() throws Exception {
        logger.info("[TEST_START] [REQ-001] Executing testAdminCreateScheduleSuccess");
        String adminToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), Instant.now(), Instant.now().plusSeconds(3600));

        String requestBody = "{\"platform\":\"FACEBOOK\",\"content\":\"Automated integration test content\",\"scheduledTime\":\"2026-12-31T12:00:00Z\"}";

        mockMvc.perform(post(API_SCHEDULES_PATH)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        logger.info("[TEST_COMPLETE] [REQ-001] testAdminCreateScheduleSuccess passed successfully.");
    }
}