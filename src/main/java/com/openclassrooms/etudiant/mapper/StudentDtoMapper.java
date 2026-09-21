package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StudentDtoMapper {

    @Mapping(target = "id", ignore = true)
    Student toEntity(StudentRequestDTO studentRequestDTO);

    StudentResponseDTO toResponseDTO(Student student);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            StudentRequestDTO studentRequestDTO,
            @MappingTarget Student student
    );
}