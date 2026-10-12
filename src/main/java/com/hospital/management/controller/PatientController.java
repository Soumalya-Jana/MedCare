package com.hospital.management.controller;

import com.hospital.management.model.Patient;
import com.hospital.management.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private com.hospital.management.repository.AppointmentRepository appointmentRepo;

    @Autowired
    private com.hospital.management.repository.PrescriptionRepository prescriptionRepo;

    @Autowired
    private com.hospital.management.repository.LabReportRepository labReportRepo;

    @Autowired
    private com.hospital.management.repository.BedRepository bedRepo;

    @Autowired
    private com.hospital.management.repository.BillRepository billRepo;

    @Autowired
    private com.hospital.management.repository.InpatientAdmissionRepository admissionRepo;

    @Autowired
    private com.hospital.management.repository.EmergencyCaseRepository emergencyRepo;

    @GetMapping
    public String listPatients(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "gender", required = false) String gender,
                               Model model) {
        java.util.List<Patient> patients;
        if (search != null && !search.trim().isEmpty()) {
            patients = patientService.searchByName(search);
        } else {
            patients = patientService.findAll();
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            patients = patients.stream()
                    .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (gender != null && !gender.trim().isEmpty() && !"ALL".equalsIgnoreCase(gender)) {
            patients = patients.stream()
                    .filter(p -> gender.equalsIgnoreCase(p.getGender()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Map patientId -> Bed for inpatient indicators
        java.util.List<com.hospital.management.model.Bed> occupiedBeds = bedRepo.findByStatus("Occupied");
        java.util.Map<Long, com.hospital.management.model.Bed> patientBedMap = new java.util.HashMap<>();
        for (com.hospital.management.model.Bed b : occupiedBeds) {
            if (b.getPatient() != null) {
                patientBedMap.put(b.getPatient().getId(), b);
            }
        }

        model.addAttribute("patients", patients);
        model.addAttribute("patientBedMap", patientBedMap);
        model.addAttribute("totalPatients", patientService.countAll());
        model.addAttribute("activePatients", patientService.countByStatus("Active"));
        model.addAttribute("dischargedPatients", patientService.countByStatus("Discharged"));
        model.addAttribute("newPatientsThisMonth", patientService.countNewPatientsThisMonth());
        model.addAttribute("admittedCount", bedRepo.countOccupiedBeds());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("search", search);

        return "patients/list";
    }

    @GetMapping({"/add", "/register", "/new"})
    public String showAddForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }

    @PostMapping({"/save", "/register"})
    public String savePatient(@ModelAttribute Patient patient, RedirectAttributes redirectAttributes) {
        boolean isNew = (patient.getId() == null);
        Patient saved = patientService.save(patient);
        redirectAttributes.addFlashAttribute("success",
                isNew ? "Patient " + saved.getFullName() + " (" + saved.getPatientId() + ") registered successfully!"
                      : "Patient " + saved.getFullName() + " updated successfully!");
        return "redirect:/patients/view/" + saved.getId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("patient", patientService.findById(id));
        return "patients/form";
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public String viewPatient(@PathVariable("id") Long id, Model model) {
        Patient patient = patientService.findById(id);
        model.addAttribute("patient", patient);

        java.util.List<com.hospital.management.model.Appointment> appointments = 
                appointmentRepo.findByPatientIdOrderByAppointmentDateDesc(id);
        java.util.List<com.hospital.management.model.Prescription> prescriptions = 
                prescriptionRepo.findByPatientIdOrderByPrescriptionDateDesc(id);
        java.util.List<com.hospital.management.model.LabReport> labReports = 
                labReportRepo.findByPatientIdOrderByTestDateDesc(id);
        java.util.List<com.hospital.management.model.Bed> assignedBeds = 
                bedRepo.findByPatientId(id);
        java.util.List<com.hospital.management.model.Bill> bills = 
                billRepo.findByPatientIdOrderByCreatedAtDesc(id);

        java.util.List<com.hospital.management.model.InpatientAdmission> inpatientAdmissions = 
                admissionRepo.findByPatientIdOrderByDischargeDateDesc(id);

        java.util.List<com.hospital.management.model.EmergencyCase> emergencyCases = 
                emergencyRepo.findByPatientIdOrderByArrivalDateTimeDesc(id);

        model.addAttribute("appointments", appointments);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("labReports", labReports);
        model.addAttribute("assignedBeds", assignedBeds);
        model.addAttribute("bills", bills);
        model.addAttribute("inpatientAdmissions", inpatientAdmissions);
        model.addAttribute("emergencyCases", emergencyCases);
        model.addAttribute("emergencyCount", emergencyCases.size());

        double totalBilled = bills.stream().mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0).sum();
        double totalPaid = bills.stream().mapToDouble(b -> b.getPaidAmount() != null ? b.getPaidAmount() : 0.0).sum();
        double totalDue = Math.max(0.0, totalBilled - totalPaid);

        model.addAttribute("totalBilled", totalBilled);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalDue", totalDue);

        model.addAttribute("appointmentCount", appointments.size());
        model.addAttribute("prescriptionCount", prescriptions.size());
        long activePrescriptionsCount = prescriptions.stream().filter(p -> "Active".equalsIgnoreCase(p.getStatus())).count();
        model.addAttribute("activePrescriptionsCount", activePrescriptionsCount);
        model.addAttribute("labCount", labReports.size());

        return "patients/view";
    }

    @GetMapping("/delete/{id}")
    public String deletePatient(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            patientService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Patient deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete patient: Active records (appointments, prescriptions, lab tests, beds, or invoices) are associated with this patient.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete patient: " + ex.getMessage());
        }
        return "redirect:/patients";
    }
}
