package com.radadmin.radadminbackend.controller;

import com.radadmin.radadminbackend.dto.CaseRequest;
import com.radadmin.radadminbackend.dto.CaseResponse;
import com.radadmin.radadminbackend.entity.CaseStatus;
import com.radadmin.radadminbackend.service.CaseService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CaseResponse> getAll(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) CaseStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return service.getAll(search, status, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public CaseResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse create(@Valid @RequestBody CaseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CaseResponse update(
        @PathVariable Long id,
        @Valid @RequestBody CaseRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}