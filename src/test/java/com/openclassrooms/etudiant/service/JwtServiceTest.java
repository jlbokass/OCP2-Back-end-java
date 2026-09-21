package com.openclassrooms.etudiant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "01234567890123456789012345678901",
                3_600_000
        );
    }

    @Test
    void generatedToken_shouldContainUsernameAndBeValid() {
        UserDetails userDetails = User
                .withUsername("agent")
                .password("ignored")
                .authorities(List.of())
                .build();

        String token =
                jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();

        assertThat(jwtService.extractUsername(token))
                .isEqualTo("agent");

        assertThat(jwtService.isTokenValid(token, userDetails))
                .isTrue();
    }

    @Test
    void token_shouldNotBeValidForAnotherUser() {
        UserDetails agent = User
                .withUsername("agent")
                .password("ignored")
                .authorities(List.of())
                .build();

        UserDetails anotherUser = User
                .withUsername("another-agent")
                .password("ignored")
                .authorities(List.of())
                .build();

        String token =
                jwtService.generateToken(agent);

        assertThat(jwtService.isTokenValid(token, anotherUser))
                .isFalse();
    }
}