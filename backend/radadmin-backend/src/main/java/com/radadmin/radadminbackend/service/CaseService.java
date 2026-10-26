package com.radadmin.radadminbackend.service;

import com.radadmin.radadminbackend.dto.CaseRequest;
import com.radadmin.radadminbackend.dto.CaseResponse;
import com.radadmin.radadminbackend.entity.BodyPart;
import com.radadmin.radadminbackend.entity.Case;
import com.radadmin.radadminbackend.entity.CaseStatus;
import com.radadmin.radadminbackend.entity.Radiologist;
import com.radadmin.radadminbackend.repository.BodyPartRepository;
import com.radadmin.radadminbackend.repository.CaseRepository;
import com.radadmin.radadminbackend.repository.RadiologistRepository;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final BodyPartRepository bodyPartRepository;
    private final RadiologistRepository radiologistRepository;

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "accessionNumber", "patientName", "createdAt",
        "updatedAt", "status", "modality"
    );

    public CaseService(
        CaseRepository caseRepository,
        BodyPartRepository bodyPartRepository,
        RadiologistRepository radiologistRepository
    ) {
        this.caseRepository = caseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.radiologistRepository = radiologistRepository;
    }

    @Transactional(readOnly = true)
    public Page<CaseResponse> getAll(
        String search, CaseStatus status, int page, int size,
        String sortBy, String direction
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid sort field");
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Direction must be asc or desc");
        }

        Pageable pageable = PageRequest.of(
            page, size, Sort.by(sortDirection, sortBy));

        String term = search == null ? "" : search.trim();
        Page<Case> results;

        if (status != null && !term.isBlank()) {
            results =
                caseRepository
                    .findByStatusAndAccessionNumberContainingIgnoreCaseOrStatusAndPatientNameContainingIgnoreCase(
                        status, term, status, term, pageable);
        } else if (status != null) {
            results = caseRepository.findByStatus(status, pageable);
        } else if (!term.isBlank()) {
            results =
                caseRepository
                    .findByAccessionNumberContainingIgnoreCaseOrPatientNameContainingIgnoreCase(
                        term, term, pageable);
        } else {
            results = caseRepository.findAll(pageable);
        }

        return results.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CaseResponse getById(Long id) {
        return toResponse(findCase(id));
    }

    @Transactional
    public CaseResponse create(CaseRequest request) {
        String accession = normalizeAccession(request.accessionNumber());

        if (caseRepository.existsByAccessionNumberIgnoreCase(accession)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Accession number already exists");
        }

        Case newCase = new Case();
        applyRequest(newCase, request, accession);

        return toResponse(caseRepository.save(newCase));
    }

    @Transactional
    public CaseResponse update(Long id, CaseRequest request) {
        Case existing = findCase(id);
        String accession = normalizeAccession(request.accessionNumber());

        if (caseRepository.existsByAccessionNumberIgnoreCaseAndIdNot(
            accession, id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Accession number already exists");
        }

        applyRequest(existing, request, accession);
        return toResponse(caseRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        caseRepository.delete(findCase(id));
    }

    private void applyRequest(
        Case target, CaseRequest request, String accession
    ) {
        BodyPart bodyPart = bodyPartRepository.findById(request.bodyPartId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Body part not found"));

        Radiologist radiologist =
            radiologistRepository.findById(request.radiologistId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Radiologist not found"));

        target.setAccessionNumber(accession);
        target.setPatientName(request.patientName().trim());
        target.setPatientDob(request.patientDob());
        target.setPatientGender(clean(request.patientGender()));
        target.setHistory(clean(request.history()));
        target.setTechnique(clean(request.technique()));
        target.setBodyPart(bodyPart);
        target.setModality(clean(request.modality()));
        target.setRadiologist(radiologist);
        target.setStatus(request.status() == null
            ? CaseStatus.INCOMPLETE : request.status());
        target.setReport(clean(request.report()));
    }

    private Case findCase(Long id) {
        return caseRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Case not found"));
    }

    private String normalizeAccession(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private CaseResponse toResponse(Case item) {
        return new CaseResponse(
            item.getId(),
            item.getAccessionNumber(),
            item.getPatientName(),
            item.getPatientDob(),
            item.getPatientGender(),
            item.getHistory(),
            item.getTechnique(),
            item.getBodyPart().getId(),
            item.getBodyPart().getName(),
            item.getModality(),
            item.getRadiologist().getId(),
            item.getRadiologist().getName(),
            item.getStatus(),
            item.getReport(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}