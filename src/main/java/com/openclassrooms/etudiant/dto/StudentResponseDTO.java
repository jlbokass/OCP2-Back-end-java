package com.openclassrooms.etudiant.dto;

public record StudentResponseDTO(
        Long id,
        String firstName,
        String lastName
) {
}