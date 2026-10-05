package com.radadmin.radadminbackend.controller;

import com.radadmin.radadminbackend.dto.BodyPartRequest;
import com.radadmin.radadminbackend.dto.BodyPartResponse;
import com.radadmin.radadminbackend.service.BodyPartService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/body-parts")
public class BodyPartController {

    private final BodyPartService service;

    public BodyPartController(BodyPartService service) {
        this.service = service;
    }

    @GetMapping
    public List<BodyPartResponse> getAll(
            @RequestParam(required = false) String search) {
        return service.getAll(search);
    }

    @GetMapping("/{id}")
    public BodyPartResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BodyPartResponse create(
            @Valid @RequestBody BodyPartRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public BodyPartResponse update(
            @PathVariable Long id,
            @Valid @RequestBody BodyPartRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}