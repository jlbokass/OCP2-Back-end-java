package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentRequestDTO(
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}