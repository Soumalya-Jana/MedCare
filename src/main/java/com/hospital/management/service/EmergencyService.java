package com.hospital.management.service;

import com.hospital.management.model.*;
import com.hospital.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service handling Emergency room triage, acute intake, vitals monitoring,
 * and transfers to inpatient wards/ICU.
 */
@Service
@Transactional
public class EmergencyService {

    @Autowired
    private EmergencyCaseRepository emergencyRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private BedRepository bedRepo;

    @Autowired
    private InpatientAdmissionRepository admissionRepo;

    public List<EmergencyCase> findAll() {
        return emergencyRepo.findByOrderByArrivalDateTimeDesc();
    }

    public List<EmergencyCase> findActiveQueue() {
        return emergencyRepo.findByStatusInOrderByTriagePriorityAscArrivalDateTimeAsc(
                Arrays.asList("TRIAGED", "IN_RESUSCITATION", "UNDER_OBSERVATION")
        );
    }

    public EmergencyCase findById(Long id) {
        return emergencyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency case not found with ID: " + id));
    }

    public EmergencyCase findByCaseNumber(String caseNumber) {
        return emergencyRepo.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException("Emergency case not found with case number: " + caseNumber));
    }

    /**
     * Registers a new emergency triage intake.
     */
    public EmergencyCase registerIntake(EmergencyCase ec, Long patientId, Long doctorId) {
        if (ec.getCaseNumber() == null || ec.getCaseNumber().trim().isEmpty()) {
            ec.setCaseNumber(generateCaseNumber());
        }

        if (patientId != null) {
            Patient patient = patientRepo.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
            ec.setPatient(patient);
            ec.setIsUnidentified(false);
            if (ec.getPatientName() == null || ec.getPatientName().trim().isEmpty()) {
                ec.setPatientName(patient.getFullName());
            }
            if (ec.getAge() == null) ec.setAge(patient.getAge());
            if (ec.getGender() == null) ec.setGender(patient.getGender());
            if (ec.getContactPhone() == null) ec.setContactPhone(patient.getPhone());
        } else {
            // Unidentified / Emergency Walk-In without prior registration
            if (Boolean.TRUE.equals(ec.getIsUnidentified())) {
                if (ec.getTemporaryIdentifier() != null && !ec.getTemporaryIdentifier().trim().isEmpty()) {
                    ec.setPatientName("[UNIDENTIFIED] " + ec.getTemporaryIdentifier());
                } else if (ec.getPatientName() == null || ec.getPatientName().trim().isEmpty()) {
                    ec.setPatientName("[UNIDENTIFIED TRAUMA PATIENT]");
                }
            }
        }

        if (doctorId != null) {
            Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
            ec.setAssignedDoctor(doctor);
        }

        if (ec.getArrivalDateTime() == null) {
            ec.setArrivalDateTime(LocalDateTime.now());
        }

        ec.syncPriority();
        return emergencyRepo.save(ec);
    }

    /**
     * Updates clinical vitals and treatment notes for a live emergency case.
     */
    public EmergencyCase updateVitals(Long id, String bp, Integer hr, Integer rr, Double spo2,
                                     Double temp, Integer gcs, Integer glucose, Integer pain,
                                     String treatmentNotes, String triageNotes) {
        EmergencyCase ec = findById(id);
        if (bp != null && !bp.trim().isEmpty()) ec.setBloodPressure(bp);
        if (hr != null) ec.setHeartRate(hr);
        if (rr != null) ec.setRespiratoryRate(rr);
        if (spo2 != null) ec.setOxygenSaturation(spo2);
        if (temp != null) ec.setTemperature(temp);
        if (gcs != null) ec.setGlasgowComaScale(gcs);
        if (glucose != null) ec.setBloodGlucose(glucose);
        if (pain != null) ec.setPainScore(pain);

        if (treatmentNotes != null && !treatmentNotes.trim().isEmpty()) {
            if (ec.getEmergencyTreatmentGiven() != null && !ec.getEmergencyTreatmentGiven().trim().isEmpty()) {
                ec.setEmergencyTreatmentGiven(ec.getEmergencyTreatmentGiven() + "\n[" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + treatmentNotes);
            } else {
                ec.setEmergencyTreatmentGiven("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + treatmentNotes);
            }
        }

        if (triageNotes != null && !triageNotes.trim().isEmpty()) {
            ec.setTriageNotes(triageNotes);
        }

        return emergencyRepo.save(ec);
    }

    /**
     * Updates emergency status (e.g. TRIAGED -> IN_RESUSCITATION -> DISCHARGED_STABLE).
     */
    public EmergencyCase updateStatus(Long id, String status, String disposition, String dispositionNotes, Long doctorId) {
        EmergencyCase ec = findById(id);
        ec.setStatus(status);
        if (disposition != null && !disposition.trim().isEmpty()) {
            ec.setDisposition(disposition);
        }
        if (dispositionNotes != null && !dispositionNotes.trim().isEmpty()) {
            ec.setDispositionNotes(dispositionNotes);
        }
        if (doctorId != null) {
            Doctor doc = doctorRepo.findById(doctorId).orElse(null);
            if (doc != null) ec.setAssignedDoctor(doc);
        }

        if (!ec.isActive()) {
            ec.setDispositionDateTime(LocalDateTime.now());
        }

        return emergencyRepo.save(ec);
    }

    /**
     * Direct admission from Emergency to an Inpatient Ward or ICU Bed.
     */
    public EmergencyCase admitToBed(Long caseId, Long bedId, String admissionReason) {
        EmergencyCase ec = findById(idCheck(caseId));
        Bed bed = bedRepo.findById(bedId)
                .orElseThrow(() -> new RuntimeException("Bed not found with ID: " + bedId));

        Patient patient = ec.getPatient();
        if (patient == null) {
            // Auto-create registered patient record for the trauma patient so billing and ward stay link smoothly
            patient = new Patient();
            patient.setFullName(ec.getPatientName());
            int ageVal = (ec.getAge() != null ? ec.getAge() : 30);
            patient.setDateOfBirth(LocalDate.now().minusYears(ageVal));
            patient.setGender(ec.getGender() != null ? ec.getGender() : "Other");
            patient.setPhone(ec.getContactPhone() != null ? ec.getContactPhone() : "ER-INTAKE");
            patient.setAddress("Emergency Intake: " + ec.getCaseNumber());
            patient.setStatus("Active");
            patient.setBloodGroup("Unknown");
            patient.setMedicalHistory("Admitted via Emergency: " + ec.getIncidentType() + " - " + ec.getChiefComplaint());
            patient = patientRepo.save(patient);
            ec.setPatient(patient);
            ec.setIsUnidentified(false);
        }

        // Assign bed
        bed.setPatient(patient);
        bed.setStatus("Occupied");
        bed.setAdmissionDate(LocalDateTime.now());
        bedRepo.save(bed);

        ec.setAdmittedBed(bed);
        String wardType = (bed.getWard() != null && bed.getWard().getWardType() != null)
                ? bed.getWard().getWardType().toUpperCase() : "GENERAL";

        if (wardType.contains("ICU") || wardType.contains("CCU")) {
            ec.setStatus("ADMITTED_ICU");
        } else {
            ec.setStatus("ADMITTED_WARD");
        }

        ec.setDisposition("Admitted to " + (bed.getWard() != null ? bed.getWard().getWardName() : "Inpatient Ward") + " (Bed " + bed.getBedNumber() + ")");
        ec.setDispositionDateTime(LocalDateTime.now());
        if (admissionReason != null && !admissionReason.trim().isEmpty()) {
            ec.setDispositionNotes(admissionReason);
        }

        return emergencyRepo.save(ec);
    }

    public void deleteById(Long id) {
        emergencyRepo.deleteById(id);
    }

    // --- KPI & Statistics ---

    public long countAll() {
        return emergencyRepo.count();
    }

    public long countActiveCases() {
        return emergencyRepo.countActiveCases();
    }

    public long countCodeRedCases() {
        return emergencyRepo.countActiveCodeRedCases();
    }

    public long countCodeOrangeCases() {
        return emergencyRepo.countActiveCodeOrangeCases();
    }

    public String generateCaseNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = emergencyRepo.count() + 1;
        int randomSuffix = (int) (Math.random() * 900) + 100;
        return String.format("ER-%s-%03d", datePart, count % 1000 + randomSuffix % 50);
    }

    private Long idCheck(Long id) {
        if (id == null) throw new IllegalArgumentException("Case ID must not be null");
        return id;
    }
}
