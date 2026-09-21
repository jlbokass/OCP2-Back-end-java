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
import static org.mockito.Mockito.*;

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

        StudentResponseDTO result =
                studentService.create(request);

        assertThat(result).isEqualTo(response);

        verify(studentRepository).save(student);
    }

    @Test
    void findAll_shouldReturnStudents() {
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

        List<StudentResponseDTO> result =
                studentService.findAll();

        assertThat(result)
                .containsExactly(adaResponse, alanResponse);
    }

    @Test
    void findById_shouldReturnStudent() {
        Student student =
                new Student(1L, "Ada", "Lovelace");

        StudentResponseDTO response =
                new StudentResponseDTO(1L, "Ada", "Lovelace");

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(studentDtoMapper.toResponseDTO(student))
                .thenReturn(response);

        StudentResponseDTO result =
                studentService.findById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void update_shouldUpdateAndReturnStudent() {
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

        StudentResponseDTO result =
                studentService.update(1L, request);

        verify(studentDtoMapper)
                .updateEntity(request, student);

        verify(studentRepository)
                .save(student);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void delete_shouldDeleteStudent() {
        Student student =
                new Student(1L, "Ada", "Lovelace");

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void findById_shouldThrowWhenStudentDoesNotExist() {
        when(studentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("99");
    }
}