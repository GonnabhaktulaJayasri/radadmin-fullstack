package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.BodyPartRequest;
import com.radadmin.radadminbackend.dto.BodyPartResponse;
import com.radadmin.radadminbackend.entity.BodyPart;
import com.radadmin.radadminbackend.repository.BodyPartRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
public class BodyPartService {

    private final BodyPartRepository repository;

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "createdAt", "updatedAt", "active");

    public BodyPartService(BodyPartRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<BodyPartResponse> getAll(
            String search,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<BodyPart> bodyParts;

        if (search == null || search.isBlank()) {
            bodyParts = repository.findAll(pageable);
        } else {
            bodyParts = repository.findByNameContainingIgnoreCase(
                    search.trim(),
                    pageable
            );
        }

        return bodyParts.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BodyPartResponse getById(Long id) {
        return toResponse(findBodyPart(id));
    }

    @Transactional
    public BodyPartResponse create(BodyPartRequest request) {
        String name = normalizeName(request.name());

        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A body part with this name already exists"
            );
        }

        BodyPart bodyPart = new BodyPart();
        bodyPart.setName(name);
        bodyPart.setActive(
                request.active() == null || request.active()
        );

        return toResponse(repository.save(bodyPart));
    }

    @Transactional
    public BodyPartResponse update(
            Long id,
            BodyPartRequest request) {

        BodyPart bodyPart = findBodyPart(id);
        String name = normalizeName(request.name());

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A body part with this name already exists"
            );
        }

        bodyPart.setName(name);

        if (request.active() != null) {
            bodyPart.setActive(request.active());
        }

        return toResponse(repository.save(bodyPart));
    }

    @Transactional
    public void delete(Long id) {
        BodyPart bodyPart = findBodyPart(id);
        repository.delete(bodyPart);
    }

    private BodyPart findBodyPart(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Body part not found"
                ));
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase(Locale.ROOT);
    }

    private BodyPartResponse toResponse(BodyPart bodyPart) {
        return new BodyPartResponse(
                bodyPart.getId(),
                bodyPart.getName(),
                bodyPart.isActive(),
                bodyPart.getCreatedAt(),
                bodyPart.getUpdatedAt()
        );
    }
}