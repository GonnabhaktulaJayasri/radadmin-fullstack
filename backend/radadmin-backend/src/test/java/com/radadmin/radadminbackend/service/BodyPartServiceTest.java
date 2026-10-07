
package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.BodyPartRequest;
import com.radadmin.radadminbackend.dto.BodyPartResponse;
import com.radadmin.radadminbackend.entity.BodyPart;
import com.radadmin.radadminbackend.repository.BodyPartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodyPartServiceTest {

    @Mock
    private BodyPartRepository repository;

    @InjectMocks
    private BodyPartService service;

    private BodyPart existingBodyPart;

    @BeforeEach
    void setUp() {
        existingBodyPart = new BodyPart();
        existingBodyPart.setId(1L);
        existingBodyPart.setName("CERVICAL SPINE");
        existingBodyPart.setActive(true);
    }

    @Test
    void createShouldSaveBodyPart() {
        BodyPartRequest request =
                new BodyPartRequest("  cervical spine  ", true);

        when(repository.existsByNameIgnoreCase("CERVICAL SPINE"))
                .thenReturn(false);

        when(repository.save(any(BodyPart.class)))
                .thenAnswer(invocation -> {
                    BodyPart saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        BodyPartResponse response = service.create(request);

        assertEquals(10L, response.id());
        assertEquals("CERVICAL SPINE", response.name());
        assertTrue(response.active());

        verify(repository).save(any(BodyPart.class));
    }

    @Test
    void createShouldRejectDuplicateName() {
        BodyPartRequest request =
                new BodyPartRequest("CERVICAL SPINE", true);

        when(repository.existsByNameIgnoreCase("CERVICAL SPINE"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any(BodyPart.class));
    }

    @Test
    void getByIdShouldReturnBodyPart() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(existingBodyPart));

        BodyPartResponse response = service.getById(1L);

        assertEquals(1L, response.id());
        assertEquals("CERVICAL SPINE", response.name());
        assertTrue(response.active());
    }

    @Test
    void getByIdShouldReturnNotFoundForMissingBodyPart() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateShouldChangeBodyPartName() {
        BodyPartRequest request =
                new BodyPartRequest("LUMBAR SPINE", false);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingBodyPart));
        when(repository.existsByNameIgnoreCaseAndIdNot(
                "LUMBAR SPINE", 1L))
                .thenReturn(false);
        when(repository.save(any(BodyPart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BodyPartResponse response = service.update(1L, request);

        assertEquals("LUMBAR SPINE", response.name());
        assertFalse(response.active());
    }

    @Test
    void deleteShouldDeleteExistingBodyPart() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(existingBodyPart));

        service.delete(1L);

        verify(repository).delete(existingBodyPart);
    }

    @Test
    void getAllShouldReturnPagedResults() {
        PageRequest pageable = PageRequest.of(0, 2);
        Page<BodyPart> page = new PageImpl<>(
                List.of(existingBodyPart), pageable, 1
        );

        when(repository.findAll(any(
                org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        Page<BodyPartResponse> result =
                service.getAll(null, 0, 2, "name", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("CERVICAL SPINE", result.getContent().get(0).name());
    }

    @Test
    void getAllShouldRejectInvalidPageSize() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getAll(null, 0, 0, "name", "asc")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
