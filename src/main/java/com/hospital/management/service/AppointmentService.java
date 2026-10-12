package com.hospital.management.service;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Appointment scheduling and management.
 */
@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    public List<Appointment> findAll() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> findByStatus(String status) {
        return appointmentRepo.findByStatus(status);
    }

    public Appointment findById(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));
    }

    /**
     * Saves an appointment, linking it to the specified patient and doctor.
     */
    public Appointment save(Appointment appointment, Long patientId, Long doctorId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        return appointmentRepo.save(appointment);
    }

    /**
     * Cancels an appointment by updating its status.
     */
    public Appointment cancel(Long id) {
        Appointment appointment = findById(id);
        appointment.setStatus("Cancelled");
        return appointmentRepo.save(appointment);
    }

    /**
     * Marks an appointment as completed.
     */
    public Appointment complete(Long id) {
        Appointment appointment = findById(id);
        appointment.setStatus("Completed");
        return appointmentRepo.save(appointment);
    }

    public void deleteById(Long id) {
        if (!appointmentRepo.existsById(id)) {
            throw new RuntimeException("Appointment not found with ID: " + id);
        }
        appointmentRepo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return appointmentRepo.count();
    }

    @Transactional(readOnly = true)
    public long countTodaysAppointments() {
        return appointmentRepo.countTodaysAppointments();
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return appointmentRepo.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findTodaysAppointments() {
        return appointmentRepo.findTodaysAppointments();
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Appointment> findTodaysAppointmentsByDoctor(Long doctorId) {
        if (doctorId == null) return java.util.Collections.emptyList();
        return appointmentRepo.findTodaysAppointmentsByDoctor(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findRecentAppointmentsByDoctor(Long doctorId) {
        if (doctorId == null) return java.util.Collections.emptyList();
        return appointmentRepo.findRecentAppointmentsByDoctor(doctorId);
    }

    @Transactional(readOnly = true)
    public long countTodaysAppointmentsByDoctor(Long doctorId) {
        if (doctorId == null) return 0;
        return appointmentRepo.countTodaysAppointmentsByDoctor(doctorId);
    }

    @Transactional(readOnly = true)
    public long countTotalAppointmentsByDoctor(Long doctorId) {
        if (doctorId == null) return 0;
        return appointmentRepo.countByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public long countCompletedAppointmentsByDoctor(Long doctorId) {
        if (doctorId == null) return 0;
        return appointmentRepo.countByDoctorIdAndStatus(doctorId, "Completed");
    }

    @Transactional(readOnly = true)
    public long countDistinctPatientsByDoctor(Long doctorId) {
        if (doctorId == null) return 0;
        return appointmentRepo.countDistinctPatientsByDoctor(doctorId);
    }
}
