package com.radadmin.radadminbackend.dto;

import com.radadmin.radadminbackend.entity.CaseStatus;
import java.time.Instant;
import java.time.LocalDate;

public record CaseResponse(
    Long id,
    String accessionNumber,
    String patientName,
    LocalDate patientDob,
    String patientGender,
    String history,
    String technique,
    Long bodyPartId,
    String bodyPartName,
    String modality,
    Long radiologistId,
    String radiologistName,
    CaseStatus status,
    String report,
    Instant createdAt,
    Instant updatedAt
) {}