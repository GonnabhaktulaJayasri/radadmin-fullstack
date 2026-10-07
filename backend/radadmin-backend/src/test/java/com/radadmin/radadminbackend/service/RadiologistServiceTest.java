package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.RadiologistRequest;
import com.radadmin.radadminbackend.dto.RadiologistResponse;
import com.radadmin.radadminbackend.entity.Radiologist;
import com.radadmin.radadminbackend.repository.RadiologistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadiologistServiceTest {

        @Mock
        private RadiologistRepository repository;

        @InjectMocks
        private RadiologistService service;

        private Radiologist radiologist;

        @BeforeEach
        void setUp() {
                radiologist = new Radiologist();
                radiologist.setId(1L);
                radiologist.setName("Dr. John Smith");
                radiologist.setEmail("john.smith@example.com");
                radiologist.setPhone("9876543210");
                radiologist.setSpecialization("Radiology");
                radiologist.setActive(true);
        }

        @Test
        void createShouldSaveRadiologist() {

                RadiologistRequest request = new RadiologistRequest(
                                "Dr. John Smith",
                                "JOHN.SMITH@EXAMPLE.COM",
                                "9876543210",
                                "Radiology",
                                true);

                when(repository.existsByEmailIgnoreCase("john.smith@example.com"))
                                .thenReturn(false);

                when(repository.save(any(Radiologist.class)))
                                .thenReturn(radiologist);

                RadiologistResponse response = service.create(request);

                assertEquals(1L, response.id());
                assertEquals("Dr. John Smith", response.name());
                assertEquals("john.smith@example.com", response.email());
                assertEquals("9876543210", response.phone());
                assertEquals("Radiology", response.specialization());
                assertTrue(response.active());

                verify(repository).save(any(Radiologist.class));
        }

        @Test
        void createShouldRejectDuplicateEmail() {

                RadiologistRequest request = new RadiologistRequest(
                                "Dr. John Smith",
                                "john@example.com",
                                null,
                                null,
                                true);

                when(repository.existsByEmailIgnoreCase("john@example.com"))
                                .thenReturn(true);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.create(request));

                assertEquals(409, exception.getStatusCode().value());

                verify(repository, never()).save(any());
        }

        @Test
        void getByIdShouldReturnRadiologist() {

                when(repository.findById(1L))
                                .thenReturn(Optional.of(radiologist));

                RadiologistResponse response = service.getById(1L);

                assertEquals(1L, response.id());
                assertEquals("Dr. John Smith", response.name());
                assertEquals("john.smith@example.com", response.email());
        }

        @Test
        void getByIdShouldReturnNotFoundForMissingRadiologist() {

                when(repository.findById(99L))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.getById(99L));

                assertEquals(404, exception.getStatusCode().value());
        }

        @Test
        void updateShouldChangeRadiologist() {

                RadiologistRequest request = new RadiologistRequest(
                                "Dr. Jane Smith",
                                "jane.smith@example.com",
                                "9999999999",
                                "Neuroradiology",
                                true);

                when(repository.findById(1L))
                                .thenReturn(Optional.of(radiologist));

                when(repository.existsByEmailIgnoreCaseAndIdNot(
                                "jane.smith@example.com",
                                1L)).thenReturn(false);

                when(repository.save(any(Radiologist.class)))
                                .thenReturn(radiologist);

                RadiologistResponse response = service.update(1L, request);

                assertEquals("Dr. Jane Smith", response.name());
                assertEquals("jane.smith@example.com", response.email());

                verify(repository).save(radiologist);
        }

        @Test
        void updateShouldRejectDuplicateEmail() {

                RadiologistRequest request = new RadiologistRequest(
                                "Dr. John Smith",
                                "other@example.com",
                                null,
                                null,
                                true);

                when(repository.findById(1L))
                                .thenReturn(Optional.of(radiologist));

                when(repository.existsByEmailIgnoreCaseAndIdNot(
                                "other@example.com",
                                1L)).thenReturn(true);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.update(1L, request));

                assertEquals(409, exception.getStatusCode().value());

                verify(repository, never()).save(any());
        }

        @Test
        void deleteShouldDeleteExistingRadiologist() {

                when(repository.findById(1L))
                                .thenReturn(Optional.of(radiologist));

                service.delete(1L);

                verify(repository).delete(radiologist);
        }

        @Test
        void getAllShouldReturnPagedResults() {

                Page<Radiologist> page = new PageImpl<>(
                                List.of(radiologist),
                                PageRequest.of(
                                                0,
                                                10,
                                                Sort.by("name").ascending()),
                                1);

                when(repository.findAll(any(PageRequest.class)))
                                .thenReturn(page);

                Page<RadiologistResponse> result = service.getAll(
                                null,
                                0,
                                10,
                                "name",
                                "asc");

                assertEquals(1, result.getTotalElements());
                assertEquals("Dr. John Smith", result.getContent().get(0).name());
        }

        @Test
        void getAllShouldSearchByNameOrEmail() {

                Page<Radiologist> page = new PageImpl<>(
                                List.of(radiologist));

                when(repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                eq("john"),
                                                eq("john"),
                                                any(PageRequest.class)))
                                .thenReturn(page);

                Page<RadiologistResponse> result = service.getAll(
                                "john",
                                0,
                                10,
                                "name",
                                "asc");

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                "john.smith@example.com",
                                result.getContent().get(0).email());
        }

        @Test
        void getAllShouldRejectInvalidPageSize() {

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.getAll(
                                                null,
                                                0,
                                                101,
                                                "name",
                                                "asc"));

                assertEquals(400, exception.getStatusCode().value());
        }
}