package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.exception.StudentNotFoundException;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentDtoMapper studentDtoMapper;

    public StudentResponseDTO create(StudentRequestDTO studentRequestDTO) {
        Student student = studentDtoMapper.toEntity(studentRequestDTO);
        Student savedStudent = studentRepository.save(student);

        return studentDtoMapper.toResponseDTO(savedStudent);
    }

    public List<StudentResponseDTO> findAll() {
        return studentRepository.findAll()
                .stream()
                .map(studentDtoMapper::toResponseDTO)
                .toList();
    }

    public StudentResponseDTO findById(Long id) {
        Student student = getStudentOrThrow(id);

        return studentDtoMapper.toResponseDTO(student);
    }

    public StudentResponseDTO update(
            Long id,
            StudentRequestDTO studentRequestDTO
    ) {
        Student student = getStudentOrThrow(id);

        studentDtoMapper.updateEntity(studentRequestDTO, student);

        Student updatedStudent = studentRepository.save(student);

        return studentDtoMapper.toResponseDTO(updatedStudent);
    }

    public void delete(Long id) {
        Student student = getStudentOrThrow(id);

        studentRepository.delete(student);
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}