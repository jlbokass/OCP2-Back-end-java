package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.exception.StudentNotFoundException;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentDtoMapper studentDtoMapper;

    @InjectMocks
    private StudentService studentService;

    @Test
    void create_shouldSaveAndReturnStudent() {
        // GIVEN: a valid request and mapper/repository responses.
        StudentRequestDTO request =
                new StudentRequestDTO("Ada", "Lovelace");

        Student student =
                new Student(null, "Ada", "Lovelace");

        Student savedStudent =
                new Student(1L, "Ada", "Lovelace");

        StudentResponseDTO response =
                new StudentResponseDTO(1L, "Ada", "Lovelace");

        when(studentDtoMapper.toEntity(request))
                .thenReturn(student);
        when(studentRepository.save(student))
                .thenReturn(savedStudent);
        when(studentDtoMapper.toResponseDTO(savedStudent))
                .thenReturn(response);

        // WHEN: a student is created.
        StudentResponseDTO result =
                studentService.create(request);

        // THEN: the persisted student is mapped to the response DTO.
        assertThat(result).isEqualTo(response);
        verify(studentRepository).save(student);
    }

    @Test
    void findAll_shouldReturnStudents() {
        // GIVEN: two students exist.
        Student ada =
                new Student(1L, "Ada", "Lovelace");
        Student alan =
                new Student(2L, "Alan", "Turing");

        StudentResponseDTO adaResponse =
                new StudentResponseDTO(1L, "Ada", "Lovelace");
        StudentResponseDTO alanResponse =
                new StudentResponseDTO(2L, "Alan", "Turing");

        when(studentRepository.findAll())
                .thenReturn(List.of(ada, alan));
        when(studentDtoMapper.toResponseDTO(ada))
                .thenReturn(adaResponse);
        when(studentDtoMapper.toResponseDTO(alan))
                .thenReturn(alanResponse);

        // WHEN: all students are requested.
        List<StudentResponseDTO> result =
                studentService.findAll();

        // THEN: both mapped students are returned in repository order.
        assertThat(result)
                .containsExactly(adaResponse, alanResponse);
    }

    @Test
    void findById_shouldReturnStudent() {
        // GIVEN: the requested student exists.
        Student student =
                new Student(1L, "Ada", "Lovelace");
        StudentResponseDTO response =
                new StudentResponseDTO(1L, "Ada", "Lovelace");

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));
        when(studentDtoMapper.toResponseDTO(student))
                .thenReturn(response);

        // WHEN: the id is requested.
        StudentResponseDTO result =
                studentService.findById(1L);

        // THEN: the mapped response is returned.
        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowWhenStudentDoesNotExist() {
        // GIVEN: the requested id does not exist.
        when(studentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // WHEN / THEN: the service reports a not-found domain error.
        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldUpdateAndReturnStudent() {
        // GIVEN: an existing student and new values.
        StudentRequestDTO request =
                new StudentRequestDTO("Augusta Ada", "Lovelace");

        Student student =
                new Student(1L, "Ada", "Lovelace");

        StudentResponseDTO response =
                new StudentResponseDTO(
                        1L,
                        "Augusta Ada",
                        "Lovelace"
                );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));
        when(studentRepository.save(student))
                .thenReturn(student);
        when(studentDtoMapper.toResponseDTO(student))
                .thenReturn(response);

        // WHEN: the student is updated.
        StudentResponseDTO result =
                studentService.update(1L, request);

        // THEN: the mapper updates the entity and the entity is persisted.
        verify(studentDtoMapper)
                .updateEntity(request, student);
        verify(studentRepository)
                .save(student);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void update_shouldThrowWhenStudentDoesNotExist() {
        // GIVEN: the requested id is absent.
        StudentRequestDTO request =
                new StudentRequestDTO("Ada", "Lovelace");

        when(studentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // WHEN / THEN: no update or save is performed.
        assertThatThrownBy(
                () -> studentService.update(99L, request)
        ).isInstanceOf(StudentNotFoundException.class);

        verify(studentDtoMapper, never())
                .updateEntity(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
        verify(studentRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void delete_shouldDeleteStudent() {
        // GIVEN: the student exists.
        Student student =
                new Student(1L, "Ada", "Lovelace");

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        // WHEN: deletion is requested.
        studentService.delete(1L);

        // THEN: the exact entity is deleted.
        verify(studentRepository).delete(student);
    }

    @Test
    void delete_shouldThrowWhenStudentDoesNotExist() {
        // GIVEN: the requested id is absent.
        when(studentRepository.findById(99L))
                .thenReturn(Optional.empty());

        // WHEN / THEN: deletion is rejected before repository.delete().
        assertThatThrownBy(
                () -> studentService.delete(99L)
        ).isInstanceOf(StudentNotFoundException.class);

        verify(studentRepository, never())
                .delete(org.mockito.ArgumentMatchers.any());
    }
}
