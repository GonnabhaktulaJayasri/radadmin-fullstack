package com.radadmin.radadminbackend.repository;

import com.radadmin.radadminbackend.entity.Case;
import com.radadmin.radadminbackend.entity.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<Case, Long> {

    boolean existsByAccessionNumberIgnoreCase(String accessionNumber);

    boolean existsByAccessionNumberIgnoreCaseAndIdNot(
        String accessionNumber, Long id
    );

    Page<Case> findByAccessionNumberContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
        String accessionNumber, String patientName, Pageable pageable
    );

    Page<Case> findByStatus(CaseStatus status, Pageable pageable);

    Page<Case> findByStatusAndAccessionNumberContainingIgnoreCaseOrStatusAndPatientNameContainingIgnoreCase(
        CaseStatus status1, String accessionNumber,
        CaseStatus status2, String patientName,
        Pageable pageable
    );
}