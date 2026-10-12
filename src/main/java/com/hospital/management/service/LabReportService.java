package com.hospital.management.service;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.LabReport;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.LabReportRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service layer for Laboratory report and medical records management.
 */
@Service
@Transactional
public class LabReportService {

    @Autowired
    private LabReportRepository labReportRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private FileStorageService fileStorageService;

    public List<LabReport> findAll() {
        return labReportRepo.findAll();
    }

    public List<LabReport> findByStatus(String status) {
        return labReportRepo.findByStatus(status);
    }

    public LabReport findById(Long id) {
        return labReportRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab report not found with ID: " + id));
    }

    /**
     * Saves a lab report without modifying attachments directly.
     */
    public LabReport save(LabReport report, Long patientId, Long doctorId) {
        return saveWithAttachment(report, patientId, doctorId, null, false);
    }

    /**
     * Saves a lab report with optional attachment upload or removal.
     */
    public LabReport saveWithAttachment(LabReport report, Long patientId, Long doctorId,
                                       MultipartFile file, boolean removeAttachment) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
        report.setPatient(patient);

        if (doctorId != null) {
            Doctor doctor = doctorRepo.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
            report.setDoctor(doctor);
        } else {
            report.setDoctor(null);
        }

        // Check if editing existing report to handle prior attachments
        if (report.getId() != null) {
            LabReport existing = labReportRepo.findById(report.getId()).orElse(null);
            if (existing != null) {
                if (removeAttachment) {
                    if (existing.getAttachmentFilePath() != null) {
                        fileStorageService.deleteFile(existing.getAttachmentFilePath());
                    }
                    report.setAttachmentFileName(null);
                    report.setAttachmentFileType(null);
                    report.setAttachmentFilePath(null);
                    report.setAttachmentFileSize(null);
                } else if (file == null || file.isEmpty()) {
                    // Retain existing attachment info if no new file is uploaded and removal wasn't requested
                    report.setAttachmentFileName(existing.getAttachmentFileName());
                    report.setAttachmentFileType(existing.getAttachmentFileType());
                    report.setAttachmentFilePath(existing.getAttachmentFilePath());
                    report.setAttachmentFileSize(existing.getAttachmentFileSize());
                } else {
                    // New file uploaded over old file
                    if (existing.getAttachmentFilePath() != null) {
                        fileStorageService.deleteFile(existing.getAttachmentFilePath());
                    }
                }
            }
        }

        // Store new file if provided
        if (file != null && !file.isEmpty()) {
            String prefix = "lab_" + (report.getId() != null ? report.getId() : "scan");
            String storedFilename = fileStorageService.storeFile(file, prefix);
            report.setAttachmentFileName(file.getOriginalFilename());
            report.setAttachmentFileType(file.getContentType());
            report.setAttachmentFilePath(storedFilename);
            report.setAttachmentFileSize(file.getSize());
        }

        return labReportRepo.save(report);
    }

    /**
     * Uploads or replaces attachment on an existing report directly.
     */
    public LabReport updateAttachment(Long reportId, MultipartFile file) {
        LabReport report = findById(reportId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided for upload.");
        }

        if (report.getAttachmentFilePath() != null) {
            fileStorageService.deleteFile(report.getAttachmentFilePath());
        }

        String storedFilename = fileStorageService.storeFile(file, "lab_" + reportId);
        report.setAttachmentFileName(file.getOriginalFilename());
        report.setAttachmentFileType(file.getContentType());
        report.setAttachmentFilePath(storedFilename);
        report.setAttachmentFileSize(file.getSize());

        return labReportRepo.save(report);
    }

    /**
     * Removes attachment from report.
     */
    public LabReport removeAttachment(Long reportId) {
        LabReport report = findById(reportId);
        if (report.getAttachmentFilePath() != null) {
            fileStorageService.deleteFile(report.getAttachmentFilePath());
        }
        report.setAttachmentFileName(null);
        report.setAttachmentFileType(null);
        report.setAttachmentFilePath(null);
        report.setAttachmentFileSize(null);
        return labReportRepo.save(report);
    }

    public void deleteById(Long id) {
        LabReport report = labReportRepo.findById(id).orElse(null);
        if (report == null) {
            throw new RuntimeException("Lab report not found with ID: " + id);
        }
        if (report.getAttachmentFilePath() != null) {
            fileStorageService.deleteFile(report.getAttachmentFilePath());
        }
        labReportRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return labReportRepo.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return labReportRepo.countByStatus(status);
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<LabReport> findReportsByDoctor(Long doctorId) {
        if (doctorId == null) return java.util.Collections.emptyList();
        return labReportRepo.findByDoctorIdOrderByTestDateDesc(doctorId);
    }

    @Transactional(readOnly = true)
    public long countPendingByDoctor(Long doctorId) {
        if (doctorId == null) return 0;
        return labReportRepo.countByDoctorIdAndStatus(doctorId, "Pending")
             + labReportRepo.countByDoctorIdAndStatus(doctorId, "In Progress");
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }
}
