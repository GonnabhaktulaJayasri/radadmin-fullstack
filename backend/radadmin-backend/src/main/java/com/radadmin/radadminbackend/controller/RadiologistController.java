package com.radadmin.radadminbackend.controller;

import com.radadmin.radadminbackend.dto.RadiologistRequest;
import com.radadmin.radadminbackend.dto.RadiologistResponse;
import com.radadmin.radadminbackend.service.RadiologistService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/radiologists")
public class RadiologistController {

    private final RadiologistService service;

    public RadiologistController(RadiologistService service) {
        this.service = service;
    }

    @GetMapping
    public Page<RadiologistResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.getAll(
                search,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{id}")
    public RadiologistResponse getById(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RadiologistResponse create(
            @Valid @RequestBody RadiologistRequest request
    ) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RadiologistResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RadiologistRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}