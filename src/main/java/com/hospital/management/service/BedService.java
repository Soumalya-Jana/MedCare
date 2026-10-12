package com.hospital.management.service;

import com.hospital.management.model.*;
import com.hospital.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service layer for Bed and Ward management operations, including inpatient admissions and discharges.
 */
@Service
@Transactional
public class BedService {

    @Autowired
    private BedRepository bedRepo;

    @Autowired
    private WardRepository wardRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private InpatientAdmissionRepository admissionRepo;

    @Autowired
    private BillRepository billRepo;

    // ---- Bed Operations ----

    public List<Bed> findAllBeds() {
        return bedRepo.findAll();
    }

    public Bed findBedById(Long id) {
        return bedRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found with ID: " + id));
    }

    /**
     * Saves a bed, linking it to the specified ward and optionally assigning a patient.
     */
    public Bed saveBed(Bed bed, Long wardId, Long patientId) {
        Ward ward = wardRepo.findById(wardId)
                .orElseThrow(() -> new RuntimeException("Ward not found with ID: " + wardId));
        bed.setWard(ward);

        if (patientId != null) {
            Patient patient = patientRepo.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
            bed.setPatient(patient);
            bed.setStatus("Occupied");
            if (bed.getAdmissionDate() == null) {
                bed.setAdmissionDate(LocalDateTime.now());
            }
        }

        return bedRepo.save(bed);
    }

    /**
     * Calculates the number of days stayed between admission and discharge.
     * Minimum duration is 1 day.
     */
    public long calculateDaysStayed(LocalDateTime admissionDate, LocalDateTime dischargeDate) {
        if (admissionDate == null) return 1L;
        if (dischargeDate == null) dischargeDate = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(admissionDate.toLocalDate(), dischargeDate.toLocalDate());
        return Math.max(1L, days);
    }

    /**
     * Comprehensive discharge workflow: records clinical discharge summary,
     * calculates room charges, optionally generates an itemized invoice, and frees the bed.
     */
    public InpatientAdmission dischargeWithSummary(Long bedId,
                                                   LocalDateTime dischargeDate,
                                                   String dischargeCondition,
                                                   String dischargeNotes,
                                                   String followUpAdvice,
                                                   String attendingDoctorName,
                                                   boolean generateBill,
                                                   Double discount,
                                                   String paymentMethod,
                                                   boolean markPaid,
                                                   String patientStatus) {
        Bed bed = findBedById(bedId);
        Patient patient = bed.getPatient();
        if (patient == null) {
            throw new RuntimeException("Bed is currently vacant; no inpatient to discharge.");
        }
        Ward ward = bed.getWard();
        LocalDateTime effectiveAdmissionDate = bed.getAdmissionDate() != null ? bed.getAdmissionDate() : LocalDateTime.now();
        LocalDateTime effectiveDischargeDate = dischargeDate != null ? dischargeDate : LocalDateTime.now();

        long daysStayed = calculateDaysStayed(effectiveAdmissionDate, effectiveDischargeDate);
        double dailyRate = bed.getDailyRate() != null ? bed.getDailyRate() : 0.0;
        double totalRoomCharges = daysStayed * dailyRate;

        Bill createdBill = null;
        if (generateBill) {
            Bill bill = new Bill();
            bill.setPatient(patient);
            bill.setRoomCharges(totalRoomCharges);
            bill.setDiscount(discount != null ? discount : 0.0);
            bill.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod : "Cash");
            bill.setDescription("Inpatient Room & Bed Charges: " + (ward != null ? ward.getWardName() : "Ward") +
                    " (" + bed.getBedNumber() + ") for " + daysStayed + " day(s).");
            bill.calculateTotal();

            if (markPaid) {
                bill.setPaidAmount(bill.getTotalAmount());
                bill.setStatus("Paid");
            } else {
                bill.setPaidAmount(0.0);
                bill.setStatus("Pending");
            }

            // Create itemized line item
            BillItem item = BillItem.builder()
                    .bill(bill)
                    .category("Room Charges")
                    .description("Inpatient Stay: " + (ward != null ? ward.getWardName() : "Ward") + " (" + bed.getBedNumber() + ")")
                    .quantity((int) daysStayed)
                    .unitPrice(dailyRate)
                    .totalPrice(totalRoomCharges)
                    .build();
            bill.getItems().add(item);
            createdBill = billRepo.save(bill);
        }

        InpatientAdmission admission = InpatientAdmission.builder()
                .patient(patient)
                .bed(bed)
                .bedNumber(bed.getBedNumber())
                .wardName(ward != null ? ward.getWardName() : "General Ward")
                .wardType(ward != null ? ward.getWardType() : "General")
                .admissionDate(effectiveAdmissionDate)
                .dischargeDate(effectiveDischargeDate)
                .daysStayed(daysStayed)
                .dailyRate(dailyRate)
                .totalRoomCharges(totalRoomCharges)
                .dischargeCondition(dischargeCondition != null ? dischargeCondition : "Stable")
                .dischargeNotes(dischargeNotes)
                .followUpAdvice(followUpAdvice)
                .attendingDoctorName(attendingDoctorName)
                .bill(createdBill)
                .build();

        InpatientAdmission savedAdmission = admissionRepo.save(admission);

        // Update patient status if specified
        if (patientStatus != null && !patientStatus.trim().isEmpty()) {
            patient.setStatus(patientStatus);
            patientRepo.save(patient);
        }

        // Release bed
        bed.setPatient(null);
        bed.setStatus("Available");
        bed.setDischargeDate(effectiveDischargeDate);
        bedRepo.save(bed);

        return savedAdmission;
    }

    /**
     * Fast discharge (retains backwards compatibility).
     */
    public Bed discharge(Long bedId) {
        Bed bed = findBedById(bedId);
        if (bed.getPatient() != null) {
            dischargeWithSummary(bedId, LocalDateTime.now(), "Stable", null, null, null, false, 0.0, "Cash", false, "Discharged");
            return findBedById(bedId);
        }
        bed.setStatus("Available");
        return bedRepo.save(bed);
    }

    public InpatientAdmission findAdmissionById(Long id) {
        return admissionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Discharge summary not found with ID: " + id));
    }

    public List<InpatientAdmission> findAdmissionsByPatientId(Long patientId) {
        return admissionRepo.findByPatientIdOrderByDischargeDateDesc(patientId);
    }

    public List<InpatientAdmission> findAllAdmissions() {
        return admissionRepo.findAllByOrderByDischargeDateDesc();
    }

    public void deleteBed(Long id) {
        if (!bedRepo.existsById(id)) {
            throw new RuntimeException("Bed not found with ID: " + id);
        }
        bedRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countAvailableBeds() {
        return bedRepo.countAvailableBeds();
    }

    @Transactional(readOnly = true)
    public long countOccupiedBeds() {
        return bedRepo.countOccupiedBeds();
    }

    @Transactional(readOnly = true)
    public long countAllBeds() {
        return bedRepo.count();
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    // ---- Ward Operations ----

    public List<Ward> findAllWards() {
        return wardRepo.findAll();
    }

    public Ward saveWard(Ward ward) {
        return wardRepo.save(ward);
    }
}
