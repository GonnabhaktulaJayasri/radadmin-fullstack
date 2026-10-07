package com.radadmin.radadminbackend.repository;

import com.radadmin.radadminbackend.entity.Radiologist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RadiologistRepository extends JpaRepository<Radiologist, Long> {

    Page<Radiologist> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}