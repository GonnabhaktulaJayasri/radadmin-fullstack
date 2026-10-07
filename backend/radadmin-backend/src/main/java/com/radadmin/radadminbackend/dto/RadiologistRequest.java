package com.radadmin.radadminbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RadiologistRequest(

        @NotBlank(message = "Radiologist name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        String phone,

        @Size(max = 150, message = "Specialization cannot exceed 150 characters")
        String specialization,

        Boolean active
) {}