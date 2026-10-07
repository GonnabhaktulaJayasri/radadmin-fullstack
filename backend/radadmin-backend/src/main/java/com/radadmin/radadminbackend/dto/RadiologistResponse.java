package com.radadmin.radadminbackend.dto;

import java.time.Instant;

public record RadiologistResponse(
        Long id,
        String name,
        String email,
        String phone,
        String specialization,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}