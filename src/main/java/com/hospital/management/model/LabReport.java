package com.hospital.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a lab report/test result for a patient,
 * with medical record and diagnostic scan file attachment support.
 */
@Entity
@Table(name = "lab_reports")
public class LabReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false, length = 100)
    private String testName;

    @Column(nullable = false, length = 50)
    private String testCategory;

    @Column(length = 2000)
    private String result;

    @Column(length = 500)
    private String normalRange;

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private Double cost = 0.0;

    @Column(nullable = false, length = 20)
    private String status = "Pending";

    private LocalDateTime testDate;

    private LocalDateTime reportDate;

    // --- File Attachment & Medical Records ---
    @Column(length = 255)
    private String attachmentFileName;

    @Column(length = 100)
    private String attachmentFileType;

    @Column(length = 500)
    private String attachmentFilePath;

    private Long attachmentFileSize;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public LabReport() {}

    public LabReport(Long id, Patient patient, Doctor doctor, String testName, String testCategory,
                     String result, String normalRange, String remarks, Double cost, String status,
                     LocalDateTime testDate, LocalDateTime reportDate, String attachmentFileName,
                     String attachmentFileType, String attachmentFilePath, Long attachmentFileSize,
                     LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.testName = testName;
        this.testCategory = testCategory;
        this.result = result;
        this.normalRange = normalRange;
        this.remarks = remarks;
        this.cost = cost != null ? cost : 0.0;
        this.status = status != null ? status : "Pending";
        this.testDate = testDate;
        this.reportDate = reportDate;
        this.attachmentFileName = attachmentFileName;
        this.attachmentFileType = attachmentFileType;
        this.attachmentFilePath = attachmentFilePath;
        this.attachmentFileSize = attachmentFileSize;
        this.createdAt = createdAt;
    }

    public static LabReportBuilder builder() {
        return new LabReportBuilder();
    }

    public static class LabReportBuilder {
        private Long id;
        private Patient patient;
        private Doctor doctor;
        private String testName;
        private String testCategory;
        private String result;
        private String normalRange;
        private String remarks;
        private Double cost = 0.0;
        private String status = "Pending";
        private LocalDateTime testDate;
        private LocalDateTime reportDate;
        private String attachmentFileName;
        private String attachmentFileType;
        private String attachmentFilePath;
        private Long attachmentFileSize;
        private LocalDateTime createdAt;

        public LabReportBuilder id(Long id) { this.id = id; return this; }
        public LabReportBuilder patient(Patient patient) { this.patient = patient; return this; }
        public LabReportBuilder doctor(Doctor doctor) { this.doctor = doctor; return this; }
        public LabReportBuilder testName(String testName) { this.testName = testName; return this; }
        public LabReportBuilder testCategory(String testCategory) { this.testCategory = testCategory; return this; }
        public LabReportBuilder result(String result) { this.result = result; return this; }
        public LabReportBuilder normalRange(String normalRange) { this.normalRange = normalRange; return this; }
        public LabReportBuilder remarks(String remarks) { this.remarks = remarks; return this; }
        public LabReportBuilder cost(Double cost) { this.cost = cost; return this; }
        public LabReportBuilder status(String status) { this.status = status; return this; }
        public LabReportBuilder testDate(LocalDateTime testDate) { this.testDate = testDate; return this; }
        public LabReportBuilder reportDate(LocalDateTime reportDate) { this.reportDate = reportDate; return this; }
        public LabReportBuilder attachmentFileName(String attachmentFileName) { this.attachmentFileName = attachmentFileName; return this; }
        public LabReportBuilder attachmentFileType(String attachmentFileType) { this.attachmentFileType = attachmentFileType; return this; }
        public LabReportBuilder attachmentFilePath(String attachmentFilePath) { this.attachmentFilePath = attachmentFilePath; return this; }
        public LabReportBuilder attachmentFileSize(Long attachmentFileSize) { this.attachmentFileSize = attachmentFileSize; return this; }
        public LabReportBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LabReport build() {
            return new LabReport(id, patient, doctor, testName, testCategory, result, normalRange,
                    remarks, cost, status, testDate, reportDate, attachmentFileName, attachmentFileType,
                    attachmentFilePath, attachmentFileSize, createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (testDate == null) {
            testDate = LocalDateTime.now();
        }
    }

    // Helper Methods
    public boolean hasAttachment() {
        return attachmentFilePath != null && !attachmentFilePath.trim().isEmpty();
    }

    public boolean isImageAttachment() {
        if (attachmentFileType != null && attachmentFileType.toLowerCase().startsWith("image/")) {
            return true;
        }
        if (attachmentFileName != null) {
            String lower = attachmentFileName.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                   lower.endsWith(".gif") || lower.endsWith(".webp");
        }
        return false;
    }

    public boolean isPdfAttachment() {
        if (attachmentFileType != null && attachmentFileType.toLowerCase().contains("pdf")) {
            return true;
        }
        if (attachmentFileName != null) {
            return attachmentFileName.toLowerCase().endsWith(".pdf");
        }
        return false;
    }

    public String getFormattedFileSize() {
        if (attachmentFileSize == null || attachmentFileSize <= 0) {
            return "0 KB";
        }
        if (attachmentFileSize < 1024) {
            return attachmentFileSize + " B";
        }
        if (attachmentFileSize < 1024 * 1024) {
            return String.format("%.1f KB", attachmentFileSize / 1024.0);
        }
        return String.format("%.2f MB", attachmentFileSize / (1024.0 * 1024.0));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getTestCategory() { return testCategory; }
    public void setTestCategory(String testCategory) { this.testCategory = testCategory; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTestDate() { return testDate; }
    public void setTestDate(LocalDateTime testDate) { this.testDate = testDate; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public String getAttachmentFileName() { return attachmentFileName; }
    public void setAttachmentFileName(String attachmentFileName) { this.attachmentFileName = attachmentFileName; }
    public String getAttachmentFileType() { return attachmentFileType; }
    public void setAttachmentFileType(String attachmentFileType) { this.attachmentFileType = attachmentFileType; }
    public String getAttachmentFilePath() { return attachmentFilePath; }
    public void setAttachmentFilePath(String attachmentFilePath) { this.attachmentFilePath = attachmentFilePath; }
    public Long getAttachmentFileSize() { return attachmentFileSize; }
    public void setAttachmentFileSize(Long attachmentFileSize) { this.attachmentFileSize = attachmentFileSize; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
