package com.hospital.management.repository;

import com.hospital.management.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate date);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByStatus(String status);
    List<Appointment> findByAppointmentDateAndDoctorId(LocalDate date, Long doctorId);
    long countByStatus(String status);
    long countByAppointmentDate(LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = CURRENT_DATE ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointments();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = CURRENT_DATE")
    long countTodaysAppointments();

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = CURRENT_DATE ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointmentsByDoctor(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findRecentAppointmentsByDoctor(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = CURRENT_DATE")
    long countTodaysAppointmentsByDoctor(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);

    long countByDoctorId(Long doctorId);

    long countByDoctorIdAndStatus(Long doctorId, String status);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countDistinctPatientsByDoctor(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);
}

