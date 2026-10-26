package com.radadmin.radadminbackend.dto;

import com.radadmin.radadminbackend.entity.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CaseRequest(
    @NotBlank(message = "Accession number is required")
    @Size(max = 100, message = "Accession number cannot exceed 100 characters")
    String accessionNumber,

    @NotBlank(message = "Patient name is required")
    @Size(max = 150, message = "Patient name cannot exceed 150 characters")
    String patientName,

    LocalDate patientDob,

    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    String patientGender,

    String history,
    String technique,

    @NotNull(message = "Body part is required")
    Long bodyPartId,

    @Size(max = 50, message = "Modality cannot exceed 50 characters")
    String modality,

    @NotNull(message = "Radiologist is required")
    Long radiologistId,

    CaseStatus status,
    String report
) {}