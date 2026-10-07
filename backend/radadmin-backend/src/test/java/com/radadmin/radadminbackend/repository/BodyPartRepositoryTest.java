package com.radadmin.radadminbackend.repository;

import com.radadmin.radadminbackend.entity.BodyPart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BodyPartRepositoryTest {

    @Autowired
    private BodyPartRepository repository;

    @BeforeEach
    void setUp() {

        repository.deleteAll();

        BodyPart chest = new BodyPart();
        chest.setName("CHEST");
        chest.setActive(true);

        BodyPart abdomen = new BodyPart();
        abdomen.setName("ABDOMEN");
        abdomen.setActive(true);

        BodyPart brain = new BodyPart();
        brain.setName("BRAIN");
        brain.setActive(false);

        repository.saveAll(List.of(chest, abdomen, brain));
    }

    // ---------------------------------------------------------
    // findByNameContainingIgnoreCase
    // ---------------------------------------------------------

    @Test
    void findByNameContainingIgnoreCaseShouldReturnMatchingBodyParts() {

        Page<BodyPart> result =
                repository.findByNameContainingIgnoreCase(
                        "che",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("name").ascending()
                        )
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0).getName())
                .isEqualTo("CHEST");
    }

    @Test
    void findByNameContainingIgnoreCaseShouldBeCaseInsensitive() {

        Page<BodyPart> result =
                repository.findByNameContainingIgnoreCase(
                        "cHe",
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0).getName())
                .isEqualTo("CHEST");
    }

    @Test
    void findByNameContainingIgnoreCaseShouldReturnEmptyWhenNoMatch() {

        Page<BodyPart> result =
                repository.findByNameContainingIgnoreCase(
                        "XYZ",
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .isEmpty();
    }

    // ---------------------------------------------------------
    // existsByNameIgnoreCase
    // ---------------------------------------------------------

    @Test
    void existsByNameIgnoreCaseShouldReturnTrueForExistingName() {

        boolean exists =
                repository.existsByNameIgnoreCase("CHEST");

        assertThat(exists)
                .isTrue();
    }

    @Test
    void existsByNameIgnoreCaseShouldBeCaseInsensitive() {

        boolean exists =
                repository.existsByNameIgnoreCase("chest");

        assertThat(exists)
                .isTrue();
    }

    @Test
    void existsByNameIgnoreCaseShouldReturnFalseForMissingName() {

        boolean exists =
                repository.existsByNameIgnoreCase("SPINE");

        assertThat(exists)
                .isFalse();
    }

    // ---------------------------------------------------------
    // existsByNameIgnoreCaseAndIdNot
    // ---------------------------------------------------------

    @Test
    void existsByNameIgnoreCaseAndIdNotShouldReturnTrueForAnotherRecord() {

        BodyPart chest =
                repository.findAll()
                        .stream()
                        .filter(bodyPart ->
                                bodyPart.getName().equals("CHEST"))
                        .findFirst()
                        .orElseThrow();

        BodyPart abdomen =
                repository.findAll()
                        .stream()
                        .filter(bodyPart ->
                                bodyPart.getName().equals("ABDOMEN"))
                        .findFirst()
                        .orElseThrow();

        boolean exists =
                repository.existsByNameIgnoreCaseAndIdNot(
                        "CHEST",
                        abdomen.getId()
                );

        assertThat(exists)
                .isTrue();

        assertThat(chest.getId())
                .isNotEqualTo(abdomen.getId());
    }

    @Test
    void existsByNameIgnoreCaseAndIdNotShouldReturnFalseForSameRecord() {

        BodyPart chest =
                repository.findAll()
                        .stream()
                        .filter(bodyPart ->
                                bodyPart.getName().equals("CHEST"))
                        .findFirst()
                        .orElseThrow();

        boolean exists =
                repository.existsByNameIgnoreCaseAndIdNot(
                        "CHEST",
                        chest.getId()
                );

        assertThat(exists)
                .isFalse();
    }

    // ---------------------------------------------------------
    // Pagination
    // ---------------------------------------------------------

    @Test
    void findByNameContainingIgnoreCaseShouldSupportPagination() {

        Page<BodyPart> firstPage =
                repository.findByNameContainingIgnoreCase(
                        "",
                        PageRequest.of(
                                0,
                                2,
                                Sort.by("name").ascending()
                        )
                );

        assertThat(firstPage.getContent())
                .hasSize(2);

        assertThat(firstPage.getTotalElements())
                .isEqualTo(3);

        assertThat(firstPage.getTotalPages())
                .isEqualTo(2);

        assertThat(firstPage.getNumber())
                .isEqualTo(0);
    }
}