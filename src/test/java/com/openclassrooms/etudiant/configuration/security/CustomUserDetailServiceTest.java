package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    void loadUserByUsername_shouldReturnUserWhenLoginExists() {
        // GIVEN: an existing user is available for the requested login.
        User user = new User();
        user.setLogin("agent");
        user.setPassword("HASHED_PASSWORD");

        when(userRepository.findByLogin("agent"))
                .thenReturn(Optional.of(user));

        // WHEN: Spring Security asks for the user.
        UserDetails result =
                customUserDetailService.loadUserByUsername("agent");

        // THEN: the repository user is returned as UserDetails.
        assertThat(result).isSameAs(user);
        assertThat(result.getUsername()).isEqualTo("agent");
    }

    @Test
    void loadUserByUsername_shouldThrowWhenLoginDoesNotExist() {
        // GIVEN: no user exists for the requested login.
        when(userRepository.findByLogin("unknown"))
                .thenReturn(Optional.empty());

        // WHEN / THEN: Spring Security receives the expected exception.
        assertThatThrownBy(
                () -> customUserDetailService.loadUserByUsername("unknown")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
