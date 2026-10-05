package com.radadmin.radadminbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BodyPartRequest(

    @NotBlank(message = "Body part name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    String name,

    Boolean active
) {
}