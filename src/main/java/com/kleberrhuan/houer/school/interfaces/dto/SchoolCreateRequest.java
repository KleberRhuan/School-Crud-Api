/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/** DTO para criação de escola. */
public record SchoolCreateRequest(
        @NotNull Long code,
        @NotBlank @Size(max = 200) String schoolName,
        @NotBlank @Size(max = 100) String administrativeDependency,
        @NotBlank @Size(max = 10) String stateCode,
        @NotBlank @Size(max = 100) String municipality,
        @NotBlank @Size(max = 100) String district,
        @NotNull Short schoolType,
        @NotBlank @Size(max = 100) String schoolTypeDescription,
        @NotNull Short situationCode,
        @NotNull Long schoolCode,
        Map<String, Long> metrics) {
}
