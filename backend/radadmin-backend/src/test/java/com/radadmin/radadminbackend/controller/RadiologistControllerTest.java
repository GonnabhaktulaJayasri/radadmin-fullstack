package com.radadmin.radadminbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.radadmin.radadminbackend.dto.RadiologistRequest;
import com.radadmin.radadminbackend.dto.RadiologistResponse;
import com.radadmin.radadminbackend.service.RadiologistService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.context.annotation.Import;

import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.radadmin.radadminbackend.exception.GlobalExceptionHandler;

@WebMvcTest(RadiologistController.class)
@Import(GlobalExceptionHandler.class)
class RadiologistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RadiologistService service;

    private RadiologistResponse response() {

        Instant now = Instant.now();

        return new RadiologistResponse(
                1L,
                "Dr. John Smith",
                "john@example.com",
                "9876543210",
                "Radiology",
                true,
                now,
                now
        );
    }

    @Test
    void getAllShouldReturnRadiologists() throws Exception {

        Page<RadiologistResponse> page =
                new PageImpl<>(List.of(response()));

        when(service.getAll(
                isNull(),
                eq(0),
                eq(10),
                eq("name"),
                eq("asc")
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/radiologists")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name")
                .value("Dr. John Smith"))
        .andExpect(jsonPath("$.content[0].email")
                .value("john@example.com"));
    }

    @Test
    void getAllShouldSupportSearchAndPagination() throws Exception {

        Page<RadiologistResponse> page =
                new PageImpl<>(List.of(response()));

        when(service.getAll(
                eq("john"),
                eq(1),
                eq(5),
                eq("email"),
                eq("desc")
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/radiologists")
                        .param("search", "john")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "email")
                        .param("direction", "desc")
        )
        .andExpect(status().isOk());

        verify(service).getAll(
                "john",
                1,
                5,
                "email",
                "desc"
        );
    }

    @Test
    void getByIdShouldReturnRadiologist() throws Exception {

        when(service.getById(1L))
                .thenReturn(response());

        mockMvc.perform(
                get("/api/radiologists/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Dr. John Smith"));
    }

    @Test
    void createShouldReturn201() throws Exception {

        RadiologistRequest request =
                new RadiologistRequest(
                        "Dr. John Smith",
                        "john@example.com",
                        "9876543210",
                        "Radiology",
                        true
                );

        when(service.create(any(RadiologistRequest.class)))
                .thenReturn(response());

        mockMvc.perform(
                post("/api/radiologists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email")
                .value("john@example.com"));

        verify(service).create(any(RadiologistRequest.class));
    }

    @Test
    void createShouldReturn400ForInvalidName() throws Exception {

        String request = """
                {
                    "name": "",
                    "email": "john@example.com"
                }
                """;

        mockMvc.perform(
                post("/api/radiologists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Radiologist name is required"));
    }

    @Test
    void createShouldReturn400ForInvalidEmail() throws Exception {

        String request = """
                {
                    "name": "Dr. John Smith",
                    "email": "invalid-email"
                }
                """;

        mockMvc.perform(
                post("/api/radiologists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Email must be valid"));
    }

    @Test
    void updateShouldReturnRadiologist() throws Exception {

        RadiologistRequest request =
                new RadiologistRequest(
                        "Dr. Jane Smith",
                        "jane@example.com",
                        "9999999999",
                        "Neuroradiology",
                        true
                );

        RadiologistResponse updated =
                new RadiologistResponse(
                        1L,
                        "Dr. Jane Smith",
                        "jane@example.com",
                        "9999999999",
                        "Neuroradiology",
                        true,
                        Instant.now(),
                        Instant.now()
                );

        when(service.update(
                eq(1L),
                any(RadiologistRequest.class)
        )).thenReturn(updated);

        mockMvc.perform(
                put("/api/radiologists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name")
                .value("Dr. Jane Smith"))
        .andExpect(jsonPath("$.email")
                .value("jane@example.com"));
    }

    @Test
    void deleteShouldReturn204() throws Exception {

        doNothing().when(service).delete(1L);

        mockMvc.perform(
                delete("/api/radiologists/1")
        )
        .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void getByIdShouldReturn404ForMissingRadiologist()
            throws Exception {

        when(service.getById(99L))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Radiologist not found"
                        )
                );

        mockMvc.perform(
                get("/api/radiologists/99")
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message")
                .value("Radiologist not found"));
    }

    @Test
    void createShouldReturn409ForDuplicateEmail()
            throws Exception {

        RadiologistRequest request =
                new RadiologistRequest(
                        "Dr. John Smith",
                        "john@example.com",
                        null,
                        null,
                        true
                );

        when(service.create(any(RadiologistRequest.class)))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.CONFLICT,
                                "A radiologist with this email already exists"
                        )
                );

        mockMvc.perform(
                post("/api/radiologists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message")
                .value("A radiologist with this email already exists"));
    }
}