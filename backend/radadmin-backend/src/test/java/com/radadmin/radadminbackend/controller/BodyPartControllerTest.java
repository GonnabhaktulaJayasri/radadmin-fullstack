package com.radadmin.radadminbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.radadmin.radadminbackend.dto.BodyPartRequest;
import com.radadmin.radadminbackend.dto.BodyPartResponse;
import com.radadmin.radadminbackend.service.BodyPartService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(BodyPartController.class)
@Import(com.radadmin.radadminbackend.exception.GlobalExceptionHandler.class)
class BodyPartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BodyPartService service;

    private final Instant createdAt = Instant.parse("2026-10-07T10:00:00Z");
    private final Instant updatedAt = Instant.parse("2026-10-07T10:00:00Z");

    // ---------------------------------------------------------
    // GET /api/body-parts
    // ---------------------------------------------------------

    @Test
    void getAllShouldReturnBodyParts() throws Exception {

        BodyPartResponse response = new BodyPartResponse(
                1L,
                "CHEST",
                true,
                createdAt,
                updatedAt);

        Page<BodyPartResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10),
                1);

        when(service.getAll(
                eq(null),
                eq(0),
                eq(10),
                eq("name"),
                eq("asc"))).thenReturn(page);

        mockMvc.perform(get("/api/body-parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("CHEST"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    // ---------------------------------------------------------
    // GET /api/body-parts/{id}
    // ---------------------------------------------------------

    @Test
    void getByIdShouldReturnBodyPart() throws Exception {

        BodyPartResponse response = new BodyPartResponse(
                1L,
                "CHEST",
                true,
                createdAt,
                updatedAt);

        when(service.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/body-parts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("CHEST"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getByIdShouldReturn404WhenBodyPartDoesNotExist() throws Exception {

        when(service.getById(999L))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Body part not found"));

        mockMvc.perform(get("/api/body-parts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Body part not found"));
    }

    // ---------------------------------------------------------
    // POST /api/body-parts
    // ---------------------------------------------------------

    @Test
    void createShouldReturn201() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "Chest",
                true);

        BodyPartResponse response = new BodyPartResponse(
                1L,
                "CHEST",
                true,
                createdAt,
                updatedAt);

        when(service.create(any(BodyPartRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/body-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("CHEST"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createShouldReturn400ForInvalidRequest() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "",
                true);

        mockMvc.perform(
                post("/api/body-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Body part name is required"));
    }

    @Test
    void createShouldReturn409ForDuplicateName() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "CHEST",
                true);

        when(service.create(any(BodyPartRequest.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "A body part with this name already exists"));

        mockMvc.perform(
                post("/api/body-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("A body part with this name already exists"));
    }

    // ---------------------------------------------------------
    // PUT /api/body-parts/{id}
    // ---------------------------------------------------------

    @Test
    void updateShouldReturnUpdatedBodyPart() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "Updated Chest",
                false);

        BodyPartResponse response = new BodyPartResponse(
                1L,
                "UPDATED CHEST",
                false,
                createdAt,
                updatedAt);

        when(service.update(eq(1L), any(BodyPartRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/body-parts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UPDATED CHEST"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateShouldReturn404WhenBodyPartDoesNotExist() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "Chest",
                true);

        when(service.update(eq(999L), any(BodyPartRequest.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Body part not found"));

        mockMvc.perform(
                put("/api/body-parts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Body part not found"));
    }

    @Test
    void updateShouldReturn409ForDuplicateName() throws Exception {

        BodyPartRequest request = new BodyPartRequest(
                "CHEST",
                true);

        when(service.update(eq(1L), any(BodyPartRequest.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "A body part with this name already exists"));

        mockMvc.perform(
                put("/api/body-parts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("A body part with this name already exists"));
    }

    // ---------------------------------------------------------
    // DELETE /api/body-parts/{id}
    // ---------------------------------------------------------

    @Test
    void deleteShouldReturn204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/body-parts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteShouldReturn404WhenBodyPartDoesNotExist() throws Exception {

        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Body part not found")).when(service).delete(999L);

        mockMvc.perform(delete("/api/body-parts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Body part not found"));
    }
}