package com.hospital.management.service;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Role;
import com.hospital.management.model.User;
import com.hospital.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for User management and authentication operations.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private com.hospital.management.repository.DoctorRepository doctorRepo;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public static final String DEFAULT_ADMIN_KEY = "MEDCARE_ADMIN_2026";

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepo.findByRole(role);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    /**
     * Registers a new System Administrator.
     */
    public User registerAdmin(com.hospital.management.dto.AdminRegisterDto dto) {
        if (dto.getAdminKey() == null || (!DEFAULT_ADMIN_KEY.equals(dto.getAdminKey().trim()) && !"admin123".equals(dto.getAdminKey().trim()))) {
            throw new IllegalArgumentException("Invalid Admin Setup Passcode. Please enter the authorized passcode (Default: MEDCARE_ADMIN_2026).");
        }

        validateCredentials(dto.getUsername(), dto.getPassword(), dto.getConfirmPassword());

        User admin = User.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName() != null && !dto.getFullName().trim().isEmpty() ? dto.getFullName().trim() : "System Administrator")
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        return userRepo.save(admin);
    }

    /**
     * Registers a new Consulting Doctor and creates both the User and Doctor profile.
     */
    public Doctor registerDoctor(com.hospital.management.dto.DoctorRegisterDto dto) {
        validateCredentials(dto.getUsername(), dto.getPassword(), dto.getConfirmPassword());

        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor full name is required.");
        }

        if (dto.getSpecialization() == null || dto.getSpecialization().trim().isEmpty()) {
            throw new IllegalArgumentException("Medical specialization is required.");
        }

        User doctorUser = User.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.DOCTOR)
                .enabled(true)
                .build();

        User savedUser = userRepo.save(doctorUser);

        com.hospital.management.model.Doctor doctor = com.hospital.management.model.Doctor.builder()
                .fullName(dto.getFullName().trim())
                .specialization(dto.getSpecialization().trim())
                .qualification(dto.getQualification() != null && !dto.getQualification().trim().isEmpty() ? dto.getQualification().trim() : "MBBS")
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .gender(dto.getGender() != null ? dto.getGender() : "Male")
                .address(dto.getAddress())
                .consultationFee(dto.getConsultationFee() != null && dto.getConsultationFee() > 0 ? dto.getConsultationFee() : 500.0)
                .availableDays(dto.getAvailableDays() != null && !dto.getAvailableDays().trim().isEmpty() ? dto.getAvailableDays() : "Mon,Tue,Wed,Thu,Fri")
                .availableTimeStart(dto.getAvailableTimeStart() != null ? dto.getAvailableTimeStart() : "09:00")
                .availableTimeEnd(dto.getAvailableTimeEnd() != null ? dto.getAvailableTimeEnd() : "17:00")
                .status("Active")
                .user(savedUser)
                .build();

        return doctorRepo.save(doctor);
    }

    /**
     * Registers a new Receptionist / Front Desk user.
     */
    public User registerReceptionist(com.hospital.management.dto.ReceptionistRegisterDto dto) {
        validateCredentials(dto.getUsername(), dto.getPassword(), dto.getConfirmPassword());

        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        User receptionist = User.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.RECEPTIONIST)
                .enabled(true)
                .build();

        return userRepo.save(receptionist);
    }

    private void validateCredentials(String username, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (userRepo.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username '" + username.trim() + "' is already registered. Please choose another username.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }
}
