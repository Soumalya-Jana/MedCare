package com.hospital.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an emergency triage record and trauma case.
 */
@Entity
@Table(name = "emergency_cases")
public class EmergencyCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String caseNumber; // e.g. ER-20261011-001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient; // Nullable for unidentified trauma intake

    @Column(nullable = false)
    private Boolean isUnidentified = false;

    @Column(length = 255)
    private String temporaryIdentifier; // e.g. "Trauma Male ~35y - Road Accident"

    @Column(nullable = false, length = 150)
    private String patientName;

    private Integer age;

    @Column(length = 20)
    private String gender;

    @Column(length = 150)
    private String contactPhone;

    /**
     * Triage Color Code & Urgency:
     * CODE_RED: Priority 1 (Immediate / Resuscitation / 0 min)
     * CODE_ORANGE: Priority 2 (Very Urgent / Emergent / <10 min)
     * CODE_YELLOW: Priority 3 (Urgent / Serious / <30 min)
     * CODE_GREEN: Priority 4 (Semi-Urgent / Less Urgent / <60 min)
     * CODE_BLUE: Priority 5 (Non-Urgent / Minor / <120 min)
     */
    @Column(nullable = false, length = 30)
    private String triageLevel = "CODE_RED";

    @Column(nullable = false)
    private Integer triagePriority = 1;

    /**
     * Status Lifecycle:
     * TRIAGED: Queued for physician / awaiting bay
     * IN_RESUSCITATION: Active care in trauma/resus bay
     * UNDER_OBSERVATION: Monitored in ER observation area
     * ADMITTED_ICU: Direct transfer to ICU
     * ADMITTED_WARD: Admitted to general/specialist ward bed
     * DISCHARGED_STABLE: Discharged home after ER treatment
     * TRANSFERRED_OUT: External tertiary referral
     */
    @Column(nullable = false, length = 40)
    private String status = "TRIAGED";

    @Column(nullable = false)
    private LocalDateTime arrivalDateTime;

    @Column(length = 40)
    private String arrivalMode = "AMBULANCE"; // AMBULANCE, WALK_IN, POLICE, AIR_AMBULANCE, TRANSFER

    @Column(length = 50)
    private String incidentType = "TRAUMA_ACCIDENT"; // TRAUMA_ACCIDENT, CARDIAC_ARREST, STROKE_NEURO, RESPIRATORY_DISTRESS, POISON_OVERDOSE, SURGICAL_ACUTE, BURNS, PEDIATRIC_EMERGENCY, GENERAL_ACUTE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;

    @Column(length = 80)
    private String triageNurse;

    @Column(length = 50)
    private String bayNumber; // e.g. "Trauma Bay 1", "Resuscitation Bed A", "Observation Bay 3"

    // --- Critical Biometrics & Clinical Vitals ---
    @Column(length = 30)
    private String bloodPressure; // e.g. "85/55"

    private Integer heartRate; // bpm

    private Integer respiratoryRate; // breaths/min

    private Double oxygenSaturation; // SpO2 percentage

    private Double temperature; // Fahrenheit

    private Integer glasgowComaScale; // GCS 3 - 15

    private Integer bloodGlucose; // mg/dL

    private Integer painScore; // 1 - 10

    @Column(length = 2000)
    private String chiefComplaint;

    @Column(length = 2000)
    private String triageNotes;

    @Column(length = 3000)
    private String emergencyTreatmentGiven;

    @Column(length = 255)
    private String disposition; // e.g. "Admitted to ICU", "Discharged Stable", "Emergency Surgery"

    @Column(length = 2000)
    private String dispositionNotes;

    private LocalDateTime dispositionDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_bed_id")
    private Bed admittedBed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public EmergencyCase() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (arrivalDateTime == null) arrivalDateTime = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        syncPriority();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        syncPriority();
    }

    public void syncPriority() {
        if (triageLevel == null) triageLevel = "CODE_RED";
        switch (triageLevel.toUpperCase()) {
            case "CODE_RED":
                this.triagePriority = 1;
                break;
            case "CODE_ORANGE":
                this.triagePriority = 2;
                break;
            case "CODE_YELLOW":
                this.triagePriority = 3;
                break;
            case "CODE_GREEN":
                this.triagePriority = 4;
                break;
            case "CODE_BLUE":
            default:
                this.triagePriority = 5;
                break;
        }
    }

    // Helper UI Predicates
    public boolean isCritical() {
        return "CODE_RED".equalsIgnoreCase(triageLevel) || "CODE_ORANGE".equalsIgnoreCase(triageLevel);
    }

    public boolean isActive() {
        return "TRIAGED".equalsIgnoreCase(status) ||
               "IN_RESUSCITATION".equalsIgnoreCase(status) ||
               "UNDER_OBSERVATION".equalsIgnoreCase(status);
    }

    public String getTriageLevelDisplayName() {
        if (triageLevel == null) return "Unknown";
        switch (triageLevel.toUpperCase()) {
            case "CODE_RED": return "Code Red (Immediate / Resuscitation)";
            case "CODE_ORANGE": return "Code Orange (Very Urgent / Emergent)";
            case "CODE_YELLOW": return "Code Yellow (Urgent)";
            case "CODE_GREEN": return "Code Green (Semi-Urgent)";
            case "CODE_BLUE": return "Code Blue (Non-Urgent)";
            default: return triageLevel;
        }
    }

    public String getTriageBadgeClass() {
        if (triageLevel == null) return "badge-secondary";
        switch (triageLevel.toUpperCase()) {
            case "CODE_RED": return "badge-code-red";
            case "CODE_ORANGE": return "badge-code-orange";
            case "CODE_YELLOW": return "badge-code-yellow";
            case "CODE_GREEN": return "badge-code-green";
            case "CODE_BLUE": return "badge-code-blue";
            default: return "badge-secondary";
        }
    }

    public String getStatusDisplayName() {
        if (status == null) return "Unknown";
        switch (status) {
            case "TRIAGED": return "Triaged / In Queue";
            case "IN_RESUSCITATION": return "In Resuscitation";
            case "UNDER_OBSERVATION": return "Under Observation";
            case "ADMITTED_ICU": return "Admitted to ICU";
            case "ADMITTED_WARD": return "Admitted to Ward";
            case "DISCHARGED_STABLE": return "Discharged Stable";
            case "TRANSFERRED_OUT": return "Transferred Out";
            default: return status;
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case "TRIAGED": return "badge-warning";
            case "IN_RESUSCITATION": return "badge-danger";
            case "UNDER_OBSERVATION": return "badge-info";
            case "ADMITTED_ICU": return "badge-primary";
            case "ADMITTED_WARD": return "badge-primary";
            case "DISCHARGED_STABLE": return "badge-success";
            case "TRANSFERRED_OUT": return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Boolean getIsUnidentified() {
        return isUnidentified;
    }

    public void setIsUnidentified(Boolean unidentified) {
        isUnidentified = unidentified;
    }

    public String getTemporaryIdentifier() {
        return temporaryIdentifier;
    }

    public void setTemporaryIdentifier(String temporaryIdentifier) {
        this.temporaryIdentifier = temporaryIdentifier;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getTriageLevel() {
        return triageLevel;
    }

    public void setTriageLevel(String triageLevel) {
        this.triageLevel = triageLevel;
        syncPriority();
    }

    public Integer getTriagePriority() {
        return triagePriority;
    }

    public void setTriagePriority(Integer triagePriority) {
        this.triagePriority = triagePriority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(LocalDateTime arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getArrivalMode() {
        return arrivalMode;
    }

    public void setArrivalMode(String arrivalMode) {
        this.arrivalMode = arrivalMode;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public Doctor getAssignedDoctor() {
        return assignedDoctor;
    }

    public void setAssignedDoctor(Doctor assignedDoctor) {
        this.assignedDoctor = assignedDoctor;
    }

    public String getTriageNurse() {
        return triageNurse;
    }

    public void setTriageNurse(String triageNurse) {
        this.triageNurse = triageNurse;
    }

    public String getBayNumber() {
        return bayNumber;
    }

    public void setBayNumber(String bayNumber) {
        this.bayNumber = bayNumber;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Double getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(Double oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getGlasgowComaScale() {
        return glasgowComaScale;
    }

    public void setGlasgowComaScale(Integer glasgowComaScale) {
        this.glasgowComaScale = glasgowComaScale;
    }

    public Integer getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(Integer bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public Integer getPainScore() {
        return painScore;
    }

    public void setPainScore(Integer painScore) {
        this.painScore = painScore;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getTriageNotes() {
        return triageNotes;
    }

    public void setTriageNotes(String triageNotes) {
        this.triageNotes = triageNotes;
    }

    public String getEmergencyTreatmentGiven() {
        return emergencyTreatmentGiven;
    }

    public void setEmergencyTreatmentGiven(String emergencyTreatmentGiven) {
        this.emergencyTreatmentGiven = emergencyTreatmentGiven;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getDispositionNotes() {
        return dispositionNotes;
    }

    public void setDispositionNotes(String dispositionNotes) {
        this.dispositionNotes = dispositionNotes;
    }

    public LocalDateTime getDispositionDateTime() {
        return dispositionDateTime;
    }

    public void setDispositionDateTime(LocalDateTime dispositionDateTime) {
        this.dispositionDateTime = dispositionDateTime;
    }

    public Bed getAdmittedBed() {
        return admittedBed;
    }

    public void setAdmittedBed(Bed admittedBed) {
        this.admittedBed = admittedBed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
