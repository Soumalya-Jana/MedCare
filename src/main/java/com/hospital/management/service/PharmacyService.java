package com.hospital.management.service;

import com.hospital.management.model.Medicine;
import com.hospital.management.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Pharmacy / Medicine inventory management.
 */
@Service
@Transactional
public class PharmacyService {

    @Autowired
    private MedicineRepository medicineRepo;

    public List<Medicine> findAll() {
        return medicineRepo.findAll();
    }

    public List<Medicine> searchByName(String name) {
        return medicineRepo.findByNameContainingIgnoreCase(name);
    }

    public Medicine findById(Long id) {
        return medicineRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + id));
    }

    public Medicine save(Medicine medicine) {
        return medicineRepo.save(medicine);
    }

    public void deleteById(Long id) {
        if (!medicineRepo.existsById(id)) {
            throw new RuntimeException("Medicine not found with ID: " + id);
        }
        medicineRepo.deleteById(id);
    }

    /**
     * Increments inventory quantity for a medicine, with optional batch update.
     */
    public Medicine restock(Long id, int quantityToAdd, String newBatchNumber) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive.");
        }
        Medicine medicine = findById(id);
        int current = medicine.getStockQuantity() != null ? medicine.getStockQuantity() : 0;
        medicine.setStockQuantity(current + quantityToAdd);
        if (newBatchNumber != null && !newBatchNumber.trim().isEmpty()) {
            medicine.setBatchNumber(newBatchNumber.trim());
        }
        return medicineRepo.save(medicine);
    }

    /**
     * Returns medicines whose stock is at or below their reorder level.
     */
    @Transactional(readOnly = true)
    public List<Medicine> findLowStockMedicines() {
        return medicineRepo.findLowStockMedicines();
    }
}
