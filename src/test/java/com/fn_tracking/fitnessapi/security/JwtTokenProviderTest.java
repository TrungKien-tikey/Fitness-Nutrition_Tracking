package com.fn_tracking.fitnessapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "01234567890123456789012345678901";

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = createProvider(SECRET, 86400000L);
    }

    @Test
    void generatedTokenShouldContainUsernameAndBeValid() {
        String token = tokenProvider.generateToken("john");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("john", tokenProvider.getUsernameFromToken(token));
    }

    @Test
    void expiredTokenShouldBeInvalid() {
        JwtTokenProvider expiredProvider = createProvider(SECRET, -1000L);
        String token = expiredProvider.generateToken("john");

        assertFalse(expiredProvider.validateToken(token));
    }

    @Test
    void tamperedTokenShouldBeInvalid() {
        String token = tokenProvider.generateToken("john");

        assertFalse(tokenProvider.validateToken(token + "tampered"));
    }

    @Test
    void tokenSignedWithDifferentSecretShouldBeInvalid() {
        String token = tokenProvider.generateToken("john");
        JwtTokenProvider otherProvider = createProvider("abcdefghijklmnopqrstuvwxyz123456", 86400000L);

        assertFalse(otherProvider.validateToken(token));
    }

    @Test
    void malformedTokenShouldBeInvalid() {
        assertFalse(tokenProvider.validateToken("not-a-jwt"));
    }

    private JwtTokenProvider createProvider(String secret, long expirationInMs) {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", expirationInMs);
        return provider;
    }
}
