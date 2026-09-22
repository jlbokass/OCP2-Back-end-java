package com.openclassrooms.etudiant.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "01234567890123456789012345678901";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000);
    }

    @Test
    void generatedToken_shouldContainUsernameAndBeValid() {
        // GIVEN: a valid authenticated user.
        UserDetails userDetails = user("agent");

        // WHEN: a token is generated.
        String token = jwtService.generateToken(userDetails);

        // THEN: it contains the username and is valid for that user.
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token))
                .isEqualTo("agent");
        assertThat(jwtService.isTokenValid(token, userDetails))
                .isTrue();
    }

    @Test
    void token_shouldNotBeValidForAnotherUser() {
        // GIVEN: a token generated for agent.
        UserDetails agent = user("agent");
        UserDetails anotherUser = user("another-agent");

        String token = jwtService.generateToken(agent);

        // WHEN / THEN: the same token is not valid for another account.
        assertThat(jwtService.isTokenValid(token, anotherUser))
                .isFalse();
    }

    @Test
    void expiredToken_shouldBeRejected() {
        // GIVEN: a service creating a token whose expiration is already past.
        JwtService expiredJwtService =
                new JwtService(SECRET, -1_000);

        UserDetails agent = user("agent");
        String token =
                expiredJwtService.generateToken(agent);

        // WHEN / THEN: parsing the expired signed token is rejected.
        assertThatThrownBy(
                () -> expiredJwtService.isTokenValid(token, agent)
        ).isInstanceOf(ExpiredJwtException.class);
    }

    private UserDetails user(String username) {
        return User.withUsername(username)
                .password("ignored")
                .authorities(List.of())
                .build();
    }
}
