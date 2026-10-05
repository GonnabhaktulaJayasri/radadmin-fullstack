package com.radadmin.radadminbackend.repository;

import com.radadmin.radadminbackend.entity.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BodyPartRepository extends JpaRepository<BodyPart, Long> {

    List<BodyPart> findByNameContainingIgnoreCaseOrderByNameAsc(
            String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id);

}
