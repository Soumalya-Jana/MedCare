package com.hospital.management.controller;

import com.hospital.management.model.Doctor;
import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private com.hospital.management.repository.AppointmentRepository appointmentRepo;

    @Autowired
    private com.hospital.management.repository.PrescriptionRepository prescriptionRepo;

    @Autowired
    private com.hospital.management.repository.LabReportRepository labReportRepo;

    @GetMapping
    public String listDoctors(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "department", required = false) String department,
                              @RequestParam(value = "status", required = false) String status,
                              Model model) {
        List<Doctor> allDoctors = doctorService.findAll();

        long totalDoctors = allDoctors.size();
        long activeDoctors = allDoctors.stream()
                .filter(d -> "Active".equalsIgnoreCase(d.getStatus()))
                .count();
        long onLeaveDoctors = allDoctors.stream()
                .filter(d -> "On Leave".equalsIgnoreCase(d.getStatus()))
                .count();

        List<String> departments = allDoctors.stream()
                .map(Doctor::getSpecialization)
                .filter(s -> s != null && !s.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        long specialtiesCount = departments.size();

        // Apply filters
        List<Doctor> filtered = allDoctors;

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(d -> (d.getFullName() != null && d.getFullName().toLowerCase().contains(q)) ||
                                 (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(q)) ||
                                 (d.getPhone() != null && d.getPhone().toLowerCase().contains(q)) ||
                                 (d.getQualification() != null && d.getQualification().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
            filtered = filtered.stream()
                    .filter(d -> department.equalsIgnoreCase(d.getSpecialization()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            filtered = filtered.stream()
                    .filter(d -> status.equalsIgnoreCase(d.getStatus()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("doctors", filtered);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("activeDoctors", activeDoctors);
        model.addAttribute("onLeaveDoctors", onLeaveDoctors);
        model.addAttribute("specialtiesCount", specialtiesCount);
        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);

        return "doctors/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctors/form";
    }

    @PostMapping("/save")
    public String saveDoctor(@ModelAttribute Doctor doctor, RedirectAttributes redirectAttributes) {
        doctorService.save(doctor);
        redirectAttributes.addFlashAttribute("success", "Doctor saved successfully!");
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("doctor", doctorService.findById(id));
        return "doctors/form";
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public String viewDoctor(@PathVariable("id") Long id, Model model) {
        Doctor doctor = doctorService.findById(id);
        model.addAttribute("doctor", doctor);

        java.util.List<com.hospital.management.model.Appointment> recentAppointments = 
                appointmentRepo.findRecentAppointmentsByDoctor(id);
        java.util.List<com.hospital.management.model.Appointment> todaysAppointments = 
                appointmentRepo.findTodaysAppointmentsByDoctor(id);
        java.util.List<com.hospital.management.model.Prescription> recentPrescriptions = 
                prescriptionRepo.findByDoctorIdOrderByPrescriptionDateDesc(id);
        java.util.List<com.hospital.management.model.LabReport> recentLabReports = 
                labReportRepo.findByDoctorIdOrderByTestDateDesc(id);

        long totalAppointments = appointmentRepo.countByDoctorId(id);
        long completedAppointments = appointmentRepo.countByDoctorIdAndStatus(id, "Completed");
        long distinctPatients = appointmentRepo.countDistinctPatientsByDoctor(id);

        model.addAttribute("recentAppointments", recentAppointments);
        model.addAttribute("todaysAppointments", todaysAppointments);
        model.addAttribute("recentPrescriptions", recentPrescriptions);
        model.addAttribute("recentLabReports", recentLabReports);
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("completedAppointments", completedAppointments);
        model.addAttribute("distinctPatients", distinctPatients);

        return "doctors/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteDoctor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Doctor deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete doctor: Active appointments, prescriptions, or lab tests are assigned to this physician.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete doctor: " + ex.getMessage());
        }
        return "redirect:/doctors";
    }
}
