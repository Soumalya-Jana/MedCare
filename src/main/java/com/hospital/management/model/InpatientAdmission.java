package com.hospital.management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an inpatient admission and discharge clinical record.
 */
@Entity
@Table(name = "inpatient_admissions")
public class InpatientAdmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private Bed bed;

    @Column(nullable = false, length = 30)
    private String bedNumber;

    @Column(nullable = false, length = 100)
    private String wardName;

    @Column(length = 50)
    private String wardType;

    @Column(nullable = false)
    private LocalDateTime admissionDate;

    @Column(nullable = false)
    private LocalDateTime dischargeDate;

    @Column(nullable = false)
    private Long daysStayed = 1L;

    @Column(nullable = false)
    private Double dailyRate = 0.0;

    @Column(nullable = false)
    private Double totalRoomCharges = 0.0;

    @Column(length = 50)
    private String dischargeCondition = "Stable"; // Stable, Recovered, Improved, Transferred, LAMA

    @Column(length = 1000)
    private String dischargeNotes;

    @Column(length = 1000)
    private String followUpAdvice;

    @Column(length = 100)
    private String attendingDoctorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public InpatientAdmission() {}

    public InpatientAdmission(Long id, Patient patient, Bed bed, String bedNumber, String wardName, String wardType,
                              LocalDateTime admissionDate, LocalDateTime dischargeDate, Long daysStayed,
                              Double dailyRate, Double totalRoomCharges, String dischargeCondition,
                              String dischargeNotes, String followUpAdvice, String attendingDoctorName,
                              Bill bill, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.bed = bed;
        this.bedNumber = bedNumber;
        this.wardName = wardName;
        this.wardType = wardType;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.daysStayed = daysStayed != null ? daysStayed : 1L;
        this.dailyRate = dailyRate != null ? dailyRate : 0.0;
        this.totalRoomCharges = totalRoomCharges != null ? totalRoomCharges : 0.0;
        this.dischargeCondition = dischargeCondition != null ? dischargeCondition : "Stable";
        this.dischargeNotes = dischargeNotes;
        this.followUpAdvice = followUpAdvice;
        this.attendingDoctorName = attendingDoctorName;
        this.bill = bill;
        this.createdAt = createdAt;
    }

    public static InpatientAdmissionBuilder builder() {
        return new InpatientAdmissionBuilder();
    }

    public static class InpatientAdmissionBuilder {
        private Long id;
        private Patient patient;
        private Bed bed;
        private String bedNumber;
        private String wardName;
        private String wardType;
        private LocalDateTime admissionDate;
        private LocalDateTime dischargeDate;
        private Long daysStayed = 1L;
        private Double dailyRate = 0.0;
        private Double totalRoomCharges = 0.0;
        private String dischargeCondition = "Stable";
        private String dischargeNotes;
        private String followUpAdvice;
        private String attendingDoctorName;
        private Bill bill;
        private LocalDateTime createdAt;

        public InpatientAdmissionBuilder id(Long id) { this.id = id; return this; }
        public InpatientAdmissionBuilder patient(Patient patient) { this.patient = patient; return this; }
        public InpatientAdmissionBuilder bed(Bed bed) { this.bed = bed; return this; }
        public InpatientAdmissionBuilder bedNumber(String bedNumber) { this.bedNumber = bedNumber; return this; }
        public InpatientAdmissionBuilder wardName(String wardName) { this.wardName = wardName; return this; }
        public InpatientAdmissionBuilder wardType(String wardType) { this.wardType = wardType; return this; }
        public InpatientAdmissionBuilder admissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; return this; }
        public InpatientAdmissionBuilder dischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; return this; }
        public InpatientAdmissionBuilder daysStayed(Long daysStayed) { this.daysStayed = daysStayed; return this; }
        public InpatientAdmissionBuilder dailyRate(Double dailyRate) { this.dailyRate = dailyRate; return this; }
        public InpatientAdmissionBuilder totalRoomCharges(Double totalRoomCharges) { this.totalRoomCharges = totalRoomCharges; return this; }
        public InpatientAdmissionBuilder dischargeCondition(String dischargeCondition) { this.dischargeCondition = dischargeCondition; return this; }
        public InpatientAdmissionBuilder dischargeNotes(String dischargeNotes) { this.dischargeNotes = dischargeNotes; return this; }
        public InpatientAdmissionBuilder followUpAdvice(String followUpAdvice) { this.followUpAdvice = followUpAdvice; return this; }
        public InpatientAdmissionBuilder attendingDoctorName(String attendingDoctorName) { this.attendingDoctorName = attendingDoctorName; return this; }
        public InpatientAdmissionBuilder bill(Bill bill) { this.bill = bill; return this; }
        public InpatientAdmissionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public InpatientAdmission build() {
            return new InpatientAdmission(id, patient, bed, bedNumber, wardName, wardType,
                    admissionDate, dischargeDate, daysStayed, dailyRate, totalRoomCharges,
                    dischargeCondition, dischargeNotes, followUpAdvice, attendingDoctorName,
                    bill, createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Bed getBed() { return bed; }
    public void setBed(Bed bed) { this.bed = bed; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }
    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }
    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
    public Long getDaysStayed() { return daysStayed; }
    public void setDaysStayed(Long daysStayed) { this.daysStayed = daysStayed; }
    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public Double getTotalRoomCharges() { return totalRoomCharges; }
    public void setTotalRoomCharges(Double totalRoomCharges) { this.totalRoomCharges = totalRoomCharges; }
    public String getDischargeCondition() { return dischargeCondition; }
    public void setDischargeCondition(String dischargeCondition) { this.dischargeCondition = dischargeCondition; }
    public String getDischargeNotes() { return dischargeNotes; }
    public void setDischargeNotes(String dischargeNotes) { this.dischargeNotes = dischargeNotes; }
    public String getFollowUpAdvice() { return followUpAdvice; }
    public void setFollowUpAdvice(String followUpAdvice) { this.followUpAdvice = followUpAdvice; }
    public String getAttendingDoctorName() { return attendingDoctorName; }
    public void setAttendingDoctorName(String attendingDoctorName) { this.attendingDoctorName = attendingDoctorName; }
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
