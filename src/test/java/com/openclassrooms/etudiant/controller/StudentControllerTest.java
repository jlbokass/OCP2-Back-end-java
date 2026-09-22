package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.repository.UserRepository;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class StudentControllerTest {

    private static final String LOGIN = "student-admin";
    private static final String PASSWORD = "password";

    @Container
    static MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private String bearerToken;

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

    @BeforeEach
    void setUpAuthenticatedUser() {
        // GIVEN: each integration test starts with one authenticated agent.
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Agent");
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);

        userService.register(user);

        bearerToken =
                "Bearer " + userService.login(LOGIN, PASSWORD);
    }

    @AfterEach
    void cleanDatabase() {
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_shouldPersistStudentAndReturnCreated()
            throws Exception {
        // GIVEN: a valid student payload.
        StudentRequestDTO request =
                new StudentRequestDTO("Ada", "Lovelace");

        // WHEN / THEN: the protected endpoint creates and returns it.
        mockMvc.perform(
                        post("/api/students")
                                .header("Authorization", bearerToken)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName")
                        .value("Ada"))
                .andExpect(jsonPath("$.lastName")
                        .value("Lovelace"));

        assertThat(studentRepository.count()).isEqualTo(1);
    }

    @Test
    void findAll_shouldReturnPersistedStudents()
            throws Exception {
        // GIVEN: two students are stored in MySQL.
        studentRepository.save(
                new Student(null, "Ada", "Lovelace")
        );
        studentRepository.save(
                new Student(null, "Alan", "Turing")
        );

        // WHEN / THEN: the protected list endpoint returns both.
        mockMvc.perform(
                        get("/api/students")
                                .header("Authorization", bearerToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void findById_shouldReturnStudent()
            throws Exception {
        // GIVEN: one known student exists.
        Student saved = studentRepository.save(
                new Student(null, "Ada", "Lovelace")
        );

        // WHEN / THEN: its detail endpoint returns the expected values.
        mockMvc.perform(
                        get("/api/students/{id}", saved.getId())
                                .header("Authorization", bearerToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(saved.getId()))
                .andExpect(jsonPath("$.firstName")
                        .value("Ada"))
                .andExpect(jsonPath("$.lastName")
                        .value("Lovelace"));
    }

    @Test
    void update_shouldModifyPersistedStudent()
            throws Exception {
        // GIVEN: an existing student and replacement values.
        Student saved = studentRepository.save(
                new Student(null, "Ada", "Lovelace")
        );

        StudentRequestDTO request =
                new StudentRequestDTO(
                        "Augusta Ada",
                        "Lovelace"
                );

        // WHEN: the protected update endpoint is called.
        mockMvc.perform(
                        put("/api/students/{id}", saved.getId())
                                .header("Authorization", bearerToken)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Augusta Ada"));

        // THEN: the MySQL row is actually updated.
        Student updated =
                studentRepository.findById(saved.getId())
                        .orElseThrow();

        assertThat(updated.getFirstName())
                .isEqualTo("Augusta Ada");
    }

    @Test
    void delete_shouldRemovePersistedStudent()
            throws Exception {
        // GIVEN: an existing student.
        Student saved = studentRepository.save(
                new Student(null, "Ada", "Lovelace")
        );

        // WHEN: the protected delete endpoint is called.
        mockMvc.perform(
                        delete("/api/students/{id}", saved.getId())
                                .header("Authorization", bearerToken)
                )
                .andExpect(status().isNoContent());

        // THEN: the row no longer exists.
        assertThat(
                studentRepository.existsById(saved.getId())
        ).isFalse();
    }

    @Test
    void findById_shouldReturnNotFoundForUnknownStudent()
            throws Exception {
        // GIVEN: id 9999 does not exist in the isolated database.

        // WHEN / THEN: the domain not-found error becomes HTTP 404.
        mockMvc.perform(
                        get("/api/students/9999")
                                .header("Authorization", bearerToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnUnauthorizedWithoutToken()
            throws Exception {
        // GIVEN: the caller has no Authorization header.

        // WHEN / THEN: Spring Security blocks the protected endpoint.
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }
}
