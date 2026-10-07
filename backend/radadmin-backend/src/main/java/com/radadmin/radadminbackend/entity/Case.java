package com.radadmin.radadminbackend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "cases",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cases_accession_number",
            columnNames = "accession_number"
        )
    }
)
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "accession_number",
        nullable = false,
        length = 100
    )
    private String accessionNumber;

    @Column(
        name = "patient_name",
        nullable = false,
        length = 150
    )
    private String patientName;

    @Column(name = "patient_dob")
    private LocalDate patientDob;

    @Column(name = "patient_gender", length = 20)
    private String patientGender;

    @Column(columnDefinition = "TEXT")
    private String history;

    @Column(columnDefinition = "TEXT")
    private String technique;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "body_part_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_cases_body_part"
        )
    )
    private BodyPart bodyPart;

    @Column(length = 50)
    private String modality;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "radiologist_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_cases_radiologist"
        )
    )
    private Radiologist radiologist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status = CaseStatus.INCOMPLETE;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getPatientDob() {
        return patientDob;
    }

    public void setPatientDob(LocalDate patientDob) {
        this.patientDob = patientDob;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(BodyPart bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public Radiologist getRadiologist() {
        return radiologist;
    }

    public void setRadiologist(Radiologist radiologist) {
        this.radiologist = radiologist;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}