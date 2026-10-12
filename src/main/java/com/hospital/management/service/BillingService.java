package com.hospital.management.service;

import com.hospital.management.model.Bill;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.BillRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Billing and invoicing operations.
 */
@Service
@Transactional
public class BillingService {

    @Autowired
    private BillRepository billRepo;

    @Autowired
    private PatientRepository patientRepo;

    public List<Bill> findAll() {
        return billRepo.findAll();
    }

    public List<Bill> findByStatus(String status) {
        return billRepo.findByStatus(status);
    }

    public Bill findById(Long id) {
        return billRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + id));
    }

    /**
     * Saves a bill, linking it to the specified patient.
     * Automatically calculates total and determines payment status.
     */
    public Bill save(Bill bill, Long patientId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
        bill.setPatient(patient);
        bill.calculateTotal();

        // Determine payment status based on paid amount
        if (bill.getPaidAmount() >= bill.getTotalAmount()) {
            bill.setStatus("Paid");
        } else if (bill.getPaidAmount() > 0) {
            bill.setStatus("Partial");
        } else {
            bill.setStatus("Pending");
        }

        return billRepo.save(bill);
    }

    public Bill recordPayment(Long billId, Double amount, String paymentMethod) {
        Bill bill = findById(billId);
        double currentPaid = bill.getPaidAmount() != null ? bill.getPaidAmount() : 0.0;
        double newPaid = currentPaid + (amount != null ? amount : 0.0);
        bill.setPaidAmount(newPaid);

        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            bill.setPaymentMethod(paymentMethod);
        }

        if (bill.getPaidAmount() >= bill.getTotalAmount()) {
            bill.setStatus("Paid");
        } else if (bill.getPaidAmount() > 0) {
            bill.setStatus("Partial");
        } else {
            bill.setStatus("Pending");
        }

        return billRepo.save(bill);
    }

    public void deleteById(Long id) {
        if (!billRepo.existsById(id)) {
            throw new RuntimeException("Bill not found with ID: " + id);
        }
        billRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        return billRepo.getTotalRevenue();
    }

    @Transactional(readOnly = true)
    public Double getTotalCollected() {
        return billRepo.getTotalCollected();
    }

    @Transactional(readOnly = true)
    public Double getTotalPending() {
        return billRepo.getTotalPending();
    }

    @Transactional(readOnly = true)
    public Double getMonthlyRevenue() {
        return billRepo.getMonthlyRevenue();
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return billRepo.countByStatus(status);
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }
}
