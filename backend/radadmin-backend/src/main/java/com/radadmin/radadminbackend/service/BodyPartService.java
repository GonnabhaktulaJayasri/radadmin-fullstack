package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.BodyPartRequest;
import com.radadmin.radadminbackend.dto.BodyPartResponse;
import com.radadmin.radadminbackend.entity.BodyPart;
import com.radadmin.radadminbackend.repository.BodyPartRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class BodyPartService {

    private final BodyPartRepository repository;

    public BodyPartService(BodyPartRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BodyPartResponse> getAll(String search) {
        List<BodyPart> bodyParts;

        if (search == null || search.isBlank()) {
            bodyParts = repository.findAll(
                org.springframework.data.domain.Sort
                    .by("name").ascending()
            );
        } else {
            bodyParts =
                repository.findByNameContainingIgnoreCaseOrderByNameAsc(
                    search.trim()
                );
        }

        return bodyParts.stream()
            .map(this::toResponse)
            .toList();
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