package com.radadmin.radadminbackend.repository;

import com.radadmin.radadminbackend.entity.Radiologist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RadiologistRepositoryTest {

        @Autowired
        private RadiologistRepository repository;

        @BeforeEach
        void setUp() {

                repository.deleteAll();
                repository.flush();

                Radiologist john = new Radiologist();
                john.setName("Dr. John Smith");
                john.setEmail("john.smith@example.com");
                john.setPhone("9876543210");
                john.setSpecialization("Radiology");
                john.setActive(true);

                Radiologist jane = new Radiologist();
                jane.setName("Dr. Jane Doe");
                jane.setEmail("jane.doe@example.com");
                jane.setPhone("9999999999");
                jane.setSpecialization("Neuroradiology");
                jane.setActive(true);

                repository.save(john);
                repository.save(jane);
        }

        @Test
        void findByNameContainingIgnoreCaseShouldReturnMatches() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<Radiologist> result = repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                "john",
                                                "john",
                                                pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                "Dr. John Smith",
                                result.getContent().get(0).getName());
        }

        @Test
        void findByEmailContainingIgnoreCaseShouldReturnMatches() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<Radiologist> result = repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                "jane.doe",
                                                "jane.doe",
                                                pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                "jane.doe@example.com",
                                result.getContent().get(0).getEmail());
        }

        @Test
        void searchShouldBeCaseInsensitive() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<Radiologist> result = repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                "JOHN",
                                                "JOHN",
                                                pageable);

                assertEquals(1, result.getTotalElements());
        }

        @Test
        void searchShouldReturnEmptyWhenNoMatch() {

                Pageable pageable = PageRequest.of(0, 10);

                Page<Radiologist> result = repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                "nobody",
                                                "nobody",
                                                pageable);

                assertTrue(result.isEmpty());
        }

        @Test
        void existsByEmailIgnoreCaseShouldReturnTrue() {

                assertTrue(
                                repository.existsByEmailIgnoreCase(
                                                "JOHN.SMITH@EXAMPLE.COM"));
        }

        @Test
        void existsByEmailIgnoreCaseShouldReturnFalseForUnknownEmail() {

                assertFalse(
                                repository.existsByEmailIgnoreCase(
                                                "unknown@example.com"));
        }

        @Test
        void existsByEmailIgnoreCaseAndIdNotShouldReturnTrueForAnotherRadiologist() {

                Radiologist john = repository
                                .findAll()
                                .stream()
                                .filter(r -> r.getEmail()
                                                .equals("john.smith@example.com"))
                                .findFirst()
                                .orElseThrow();

                assertTrue(
                                repository.existsByEmailIgnoreCaseAndIdNot(
                                                "jane.doe@example.com",
                                                john.getId()));
        }

        @Test
        void existsByEmailIgnoreCaseAndIdNotShouldReturnFalseForSameRadiologist() {

                Radiologist john = repository
                                .findAll()
                                .stream()
                                .filter(r -> r.getEmail()
                                                .equals("john.smith@example.com"))
                                .findFirst()
                                .orElseThrow();

                assertFalse(
                                repository.existsByEmailIgnoreCaseAndIdNot(
                                                "john.smith@example.com",
                                                john.getId()));
        }

        @Test
        void searchShouldSupportPagination() {

                Pageable pageable = PageRequest.of(0, 1);

                Page<Radiologist> result = repository
                                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                                                "Dr.",
                                                "Dr.",
                                                pageable);

                assertEquals(2, result.getTotalElements());
                assertEquals(1, result.getContent().size());
                assertEquals(2, result.getTotalPages());
        }
}