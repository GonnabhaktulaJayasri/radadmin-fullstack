package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.RadiologistRequest;
import com.radadmin.radadminbackend.dto.RadiologistResponse;
import com.radadmin.radadminbackend.entity.Radiologist;
import com.radadmin.radadminbackend.repository.RadiologistRepository;

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
public class RadiologistService {

    private final RadiologistRepository repository;

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "email", "specialization", "createdAt", "updatedAt", "active");

    public RadiologistService(RadiologistRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<RadiologistResponse> getAll(
            String search,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
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

        Page<Radiologist> radiologists;

        if (search == null || search.isBlank()) {
            radiologists = repository.findAll(pageable);
        } else {
            String normalizedSearch = search.trim();

            radiologists = repository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            normalizedSearch,
                            normalizedSearch,
                            pageable
                    );
        }

        return radiologists.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RadiologistResponse getById(Long id) {
        return toResponse(findRadiologist(id));
    }

    @Transactional
    public RadiologistResponse create(RadiologistRequest request) {

        String name = normalizeName(request.name());
        String email = normalizeEmail(request.email());

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A radiologist with this email already exists"
            );
        }

        Radiologist radiologist = new Radiologist();

        radiologist.setName(name);
        radiologist.setEmail(email);
        radiologist.setPhone(normalizeOptional(request.phone()));
        radiologist.setSpecialization(normalizeOptional(request.specialization()));
        radiologist.setActive(
                request.active() == null || request.active()
        );

        return toResponse(repository.save(radiologist));
    }

    @Transactional
    public RadiologistResponse update(Long id, RadiologistRequest request) {

        Radiologist radiologist = findRadiologist(id);

        String name = normalizeName(request.name());
        String email = normalizeEmail(request.email());

        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A radiologist with this email already exists"
            );
        }

        radiologist.setName(name);
        radiologist.setEmail(email);
        radiologist.setPhone(normalizeOptional(request.phone()));
        radiologist.setSpecialization(normalizeOptional(request.specialization()));

        if (request.active() != null) {
            radiologist.setActive(request.active());
        }

        return toResponse(repository.save(radiologist));
    }

    @Transactional
    public void delete(Long id) {
        Radiologist radiologist = findRadiologist(id);
        repository.delete(radiologist);
    }

    private Radiologist findRadiologist(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Radiologist not found"
                        )
                );
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private RadiologistResponse toResponse(Radiologist radiologist) {
        return new RadiologistResponse(
                radiologist.getId(),
                radiologist.getName(),
                radiologist.getEmail(),
                radiologist.getPhone(),
                radiologist.getSpecialization(),
                radiologist.isActive(),
                radiologist.getCreatedAt(),
                radiologist.getUpdatedAt()
        );
    }
}