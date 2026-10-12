package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private com.hospital.management.repository.PrescriptionRepository prescriptionRepo;

    @Autowired
    private com.hospital.management.repository.LabReportRepository labReportRepo;

    @Autowired
    private com.hospital.management.repository.AppointmentRepository appointmentRepo;

    @Autowired
    private com.hospital.management.repository.DoctorRepository doctorRepo;

    @Autowired
    private com.hospital.management.repository.PatientRepository patientRepo;

    @GetMapping
    public String listAppointments(@RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "filter", required = false) String filter,
                                   @RequestParam(value = "doctorId", required = false) Long doctorId,
                                   Model model) {
        java.util.List<Appointment> appointments;

        if ("today".equalsIgnoreCase(filter)) {
            if (doctorId != null) {
                appointments = appointmentService.findTodaysAppointmentsByDoctor(doctorId);
            } else {
                appointments = appointmentService.findTodaysAppointments();
            }
        } else if (doctorId != null) {
            appointments = appointmentRepo.findRecentAppointmentsByDoctor(doctorId);
        } else {
            appointments = appointmentService.findAll();
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            appointments = appointments.stream()
                    .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("totalAppointments", appointmentService.countAll());
        model.addAttribute("todaysAppointmentsCount", appointmentRepo.countTodaysAppointments());
        model.addAttribute("scheduledCount", appointmentService.countByStatus("Scheduled"));
        model.addAttribute("completedCount", appointmentService.countByStatus("Completed"));
        model.addAttribute("cancelledCount", appointmentService.countByStatus("Cancelled"));
        model.addAttribute("doctors", appointmentService.getAllDoctors());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedFilter", filter);
        model.addAttribute("selectedDoctorId", doctorId);

        return "appointments/list";
    }

    @GetMapping({"/add", "/book", "/new"})
    public String showAddForm(@RequestParam(value = "patientId", required = false) Long patientId,
                              @RequestParam(value = "doctorId", required = false) Long doctorId,
                              Model model) {
        Appointment appointment = new Appointment();

        if (patientId != null) {
            patientRepo.findById(patientId).ifPresent(p -> {
                appointment.setPatient(p);
                model.addAttribute("preSelectedPatient", p);
            });
        }

        if (doctorId != null) {
            doctorRepo.findById(doctorId).ifPresent(d -> {
                appointment.setDoctor(d);
                model.addAttribute("preSelectedDoctor", d);
            });
        }

        // Default appointment date to today if creating new
        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(java.time.LocalDate.now());
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("patients", appointmentService.getAllPatients());
        model.addAttribute("doctors", appointmentService.getAllDoctors());
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("selectedDoctorId", doctorId);
        return "appointments/form";
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public String viewAppointment(@PathVariable("id") Long id, Model model) {
        Appointment appointment = appointmentService.findById(id);
        model.addAttribute("appointment", appointment);

        if (appointment.getPatient() != null) {
            model.addAttribute("patientPrescriptions", 
                prescriptionRepo.findByPatientIdOrderByPrescriptionDateDesc(appointment.getPatient().getId()));
            model.addAttribute("patientLabReports", 
                labReportRepo.findByPatientIdOrderByTestDateDesc(appointment.getPatient().getId()));
        }
        return "appointments/view";
    }

    @PostMapping({"/save", "/book"})
    public String saveAppointment(@ModelAttribute Appointment appointment,
                                  @RequestParam("patientId") Long patientId,
                                  @RequestParam("doctorId") Long doctorId,
                                  RedirectAttributes redirectAttributes) {
        Appointment saved = appointmentService.save(appointment, patientId, doctorId);
        redirectAttributes.addFlashAttribute("success", "Appointment booked successfully for " + 
                (saved.getPatient() != null ? saved.getPatient().getFullName() : "patient") + "!");
        return "redirect:/appointments/view/" + saved.getId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Appointment appointment = appointmentService.findById(id);
        model.addAttribute("appointment", appointment);
        model.addAttribute("patients", appointmentService.getAllPatients());
        model.addAttribute("doctors", appointmentService.getAllDoctors());
        if (appointment.getPatient() != null) {
            model.addAttribute("selectedPatientId", appointment.getPatient().getId());
            model.addAttribute("preSelectedPatient", appointment.getPatient());
        }
        if (appointment.getDoctor() != null) {
            model.addAttribute("selectedDoctorId", appointment.getDoctor().getId());
            model.addAttribute("preSelectedDoctor", appointment.getDoctor());
        }
        return "appointments/form";
    }

    @GetMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable("id") Long id,
                                    @RequestParam(value = "redirect", required = false) String redirect,
                                    RedirectAttributes redirectAttributes) {
        appointmentService.cancel(id);
        redirectAttributes.addFlashAttribute("success", "Appointment cancelled!");
        return "redirect:" + (redirect != null && !redirect.trim().isEmpty() ? redirect : "/appointments");
    }

    @GetMapping("/complete/{id}")
    public String completeAppointment(@PathVariable("id") Long id,
                                      @RequestParam(value = "redirect", required = false) String redirect,
                                      RedirectAttributes redirectAttributes) {
        appointmentService.complete(id);
        redirectAttributes.addFlashAttribute("success", "Appointment marked as completed!");
        return "redirect:" + (redirect != null && !redirect.trim().isEmpty() ? redirect : "/appointments");
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Appointment deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete appointment: Linked medical consultations or billing records exist.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete appointment: " + ex.getMessage());
        }
        return "redirect:/appointments";
    }
}
