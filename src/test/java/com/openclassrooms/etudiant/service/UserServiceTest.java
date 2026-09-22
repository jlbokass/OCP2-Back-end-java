package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String HASHED_PASSWORD = "HASHED_PASSWORD";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldRejectNullUser() {
        // GIVEN / WHEN / THEN: a null user is not a valid registration input.
        assertThatThrownBy(() -> userService.register(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_shouldRejectDuplicateLogin() {
        // GIVEN: the requested login already exists.
        User user = buildUser(PASSWORD);

        when(userRepository.findByLogin(LOGIN))
                .thenReturn(Optional.of(user));

        // WHEN / THEN: registration is rejected before encoding or saving.
        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(LOGIN);

        verify(passwordEncoder, never()).encode(PASSWORD);
        verify(userRepository, never()).save(user);
    }

    @Test
    void register_shouldEncodePasswordAndSaveUser() {
        // GIVEN: a new login and a password encoder.
        User user = buildUser(PASSWORD);

        when(userRepository.findByLogin(LOGIN))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(HASHED_PASSWORD);

        // WHEN: the user is registered.
        userService.register(user);

        // THEN: the password is encoded before persistence.
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPassword())
                .isEqualTo(HASHED_PASSWORD);
    }

    @Test
    void login_shouldReturnJwtForValidCredentials() {
        // GIVEN: the login exists and the supplied password matches.
        User user = buildUser(HASHED_PASSWORD);

        when(userRepository.findByLogin(LOGIN))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("JWT_TOKEN");

        // WHEN: authentication is requested.
        String token = userService.login(LOGIN, PASSWORD);

        // THEN: a JWT generated for the authenticated user is returned.
        assertThat(token).isEqualTo("JWT_TOKEN");

        verify(passwordEncoder)
                .matches(PASSWORD, HASHED_PASSWORD);

        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldRejectUnknownLogin() {
        // GIVEN: the login does not exist.
        when(userRepository.findByLogin("unknown"))
                .thenReturn(Optional.empty());

        // WHEN / THEN: authentication is rejected and no JWT is generated.
        assertThatThrownBy(
                () -> userService.login("unknown", PASSWORD)
        ).isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never())
                .generateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        // GIVEN: the user exists but the password does not match.
        User user = buildUser(HASHED_PASSWORD);

        when(userRepository.findByLogin(LOGIN))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WRONG_PASSWORD", HASHED_PASSWORD))
                .thenReturn(false);

        // WHEN / THEN: authentication is rejected and no JWT is generated.
        assertThatThrownBy(
                () -> userService.login(LOGIN, "WRONG_PASSWORD")
        ).isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(user);
    }

    private User buildUser(String password) {
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(password);
        return user;
    }
}
