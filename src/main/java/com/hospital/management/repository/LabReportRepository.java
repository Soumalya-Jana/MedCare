package com.hospital.management.repository;

import com.hospital.management.model.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientId(Long patientId);
    List<LabReport> findByPatientIdOrderByTestDateDesc(Long patientId);
    List<LabReport> findByDoctorId(Long doctorId);
    List<LabReport> findByStatus(String status);
    List<LabReport> findByTestCategory(String testCategory);
    long countByStatus(String status);

    @org.springframework.data.jpa.repository.Query("SELECT lr FROM LabReport lr WHERE lr.doctor.id = :doctorId ORDER BY lr.testDate DESC")
    List<LabReport> findByDoctorIdOrderByTestDateDesc(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    long countByDoctorIdAndStatus(Long doctorId, String status);
}

