package com.radadmin.radadminbackend.dto;

import java.time.Instant;

public record BodyPartResponse(
    Long id,
    String name,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}