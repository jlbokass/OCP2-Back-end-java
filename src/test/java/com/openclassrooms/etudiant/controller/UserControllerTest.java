package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class UserControllerTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    @Container
    static MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:8.4");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureTestProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                mySQLContainer::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                mySQLContainer::getUsername
        );
        registry.add(
                "spring.datasource.password",
                mySQLContainer::getPassword
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create"
        );
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldReturnBadRequestWhenRequiredDataIsMissing()
            throws Exception {
        // GIVEN: an empty registration DTO.
        RegisterDTO request = new RegisterDTO();

        // WHEN / THEN: Bean Validation rejects the request.
        mockMvc.perform(
                        post("/api/register")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnBadRequestWhenLoginAlreadyExists()
            throws Exception {
        // GIVEN: an existing account uses the requested login.
        userService.register(buildUser());

        RegisterDTO request = buildRegisterDTO();

        // WHEN / THEN: the duplicate registration is rejected.
        mockMvc.perform(
                        post("/api/register")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldCreateAndPersistUser()
            throws Exception {
        // GIVEN: a valid registration payload.
        RegisterDTO request = buildRegisterDTO();

        // WHEN: the registration endpoint is called.
        mockMvc.perform(
                        post("/api/register")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        // THEN: the new account exists in the temporary MySQL database.
        User savedUser =
                userRepository.findByLogin(LOGIN)
                        .orElseThrow();

        assertThat(savedUser.getFirstName())
                .isEqualTo(FIRST_NAME);
        assertThat(savedUser.getLastName())
                .isEqualTo(LAST_NAME);
        assertThat(savedUser.getPassword())
                .isNotEqualTo(PASSWORD);
    }

    @Test
    void login_shouldReturnJwtForValidCredentials()
            throws Exception {
        // GIVEN: an account registered with a BCrypt password.
        userService.register(buildUser());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setLogin(LOGIN);
        request.setPassword(PASSWORD);

        // WHEN / THEN: valid credentials return a non-empty token.
        mockMvc.perform(
                        post("/api/login")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_shouldReturnUnauthorizedForInvalidPassword()
            throws Exception {
        // GIVEN: an existing account and an invalid password.
        userService.register(buildUser());

        LoginRequestDTO request = new LoginRequestDTO();
        request.setLogin(LOGIN);
        request.setPassword("wrong-password");

        // WHEN / THEN: authentication is rejected.
        mockMvc.perform(
                        post("/api/login")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnBadRequestWhenPasswordIsMissing()
            throws Exception {
        // GIVEN: a login request without its required password.
        LoginRequestDTO request = new LoginRequestDTO();
        request.setLogin(LOGIN);

        // WHEN / THEN: Bean Validation returns HTTP 400.
        mockMvc.perform(
                        post("/api/login")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    private User buildUser() {
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        return user;
    }

    private RegisterDTO buildRegisterDTO() {
        RegisterDTO dto = new RegisterDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setLogin(LOGIN);
        dto.setPassword(PASSWORD);
        return dto;
    }
}
