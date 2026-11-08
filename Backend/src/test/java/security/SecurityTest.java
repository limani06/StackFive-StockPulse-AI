package org.example.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void generatedToken_containsCorrectEmailAndRole() {
        String token = jwtUtil.generateToken("owner@stackfive.com", "OWNER");

        assertEquals("owner@stackfive.com", jwtUtil.extractEmail(token));
        assertEquals("OWNER", jwtUtil.extractRole(token));
    }

    @Test
    void validToken_passesValidation() {
        String token = jwtUtil.generateToken("staff@stackfive.com", "STAFF");

        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void malformedToken_failsValidation() {
        String fakeToken = "this.is.not.a.real.token";

        assertFalse(jwtUtil.isTokenValid(fakeToken));
    }

    @Test
    void differentRoles_produceDifferentTokens() {
        String ownerToken = jwtUtil.generateToken("same@stackfive.com", "OWNER");
        String staffToken = jwtUtil.generateToken("same@stackfive.com", "STAFF");

        assertNotEquals(ownerToken, staffToken);
        assertEquals("OWNER", jwtUtil.extractRole(ownerToken));
        assertEquals("STAFF", jwtUtil.extractRole(staffToken));
    }
}
