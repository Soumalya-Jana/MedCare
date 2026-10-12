package com.hospital.management.controller;

import com.hospital.management.model.Bed;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.EmergencyCase;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.BedRepository;
import com.hospital.management.service.DoctorService;
import com.hospital.management.service.EmergencyService;
import com.hospital.management.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private BedRepository bedRepo;

    /**
     * ER Command Center & Live Triage Board
     */
    @GetMapping
    public String commandCenter(@RequestParam(value = "filter", defaultValue = "ACTIVE") String filter,
                                @RequestParam(value = "triage", required = false) String triage,
                                Model model) {
        List<EmergencyCase> cases;

        if ("ALL".equalsIgnoreCase(filter)) {
            cases = emergencyService.findAll();
        } else if ("DISCHARGED".equalsIgnoreCase(filter)) {
            cases = emergencyService.findAll().stream()
                    .filter(c -> "DISCHARGED_STABLE".equalsIgnoreCase(c.getStatus()) || "TRANSFERRED_OUT".equalsIgnoreCase(c.getStatus()))
                    .collect(Collectors.toList());
        } else if ("ADMITTED".equalsIgnoreCase(filter)) {
            cases = emergencyService.findAll().stream()
                    .filter(c -> "ADMITTED_ICU".equalsIgnoreCase(c.getStatus()) || "ADMITTED_WARD".equalsIgnoreCase(c.getStatus()))
                    .collect(Collectors.toList());
        } else { // ACTIVE by default
            cases = emergencyService.findActiveQueue();
        }

        if (triage != null && !triage.trim().isEmpty() && !"ALL".equalsIgnoreCase(triage)) {
            cases = cases.stream()
                    .filter(c -> triage.equalsIgnoreCase(c.getTriageLevel()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("cases", cases);
        model.addAttribute("activeQueue", emergencyService.findActiveQueue());
        model.addAttribute("totalCases", emergencyService.countAll());
        model.addAttribute("activeCases", emergencyService.countActiveCases());
        model.addAttribute("codeRedCount", emergencyService.countCodeRedCases());
        model.addAttribute("codeOrangeCount", emergencyService.countCodeOrangeCases());
        model.addAttribute("selectedFilter", filter);
        model.addAttribute("selectedTriage", triage);

        // Quick bed availability for fast triage escalation
        long availableBeds = bedRepo.countAvailableBeds();
        model.addAttribute("availableBedsCount", availableBeds);

        return "emergency/board";
    }

    /**
     * Rapid Emergency Intake & Triage Form
     */
    @GetMapping({"/intake", "/new"})
    public String showIntakeForm(@RequestParam(value = "patientId", required = false) Long patientId,
                                 @RequestParam(value = "code", defaultValue = "CODE_RED") String code,
                                 Model model) {
        EmergencyCase emergencyCase = new EmergencyCase();
        emergencyCase.setTriageLevel(code);
        emergencyCase.syncPriority();

        if (patientId != null) {
            try {
                Patient p = patientService.findById(patientId);
                emergencyCase.setPatient(p);
                emergencyCase.setPatientName(p.getFullName());
                emergencyCase.setAge(p.getAge());
                emergencyCase.setGender(p.getGender());
                emergencyCase.setContactPhone(p.getPhone());
            } catch (Exception ignored) {
            }
        }

        List<Patient> patients = patientService.findAll();
        List<Doctor> doctors = doctorService.findAll();
        List<String> bayPresets = Arrays.asList(
                "Trauma Bay 1 (Resuscitation)",
                "Trauma Bay 2 (Cardiac / Major)",
                "ER Resuscitation Bed A",
                "ER Resuscitation Bed B",
                "Acute Observation Bay 1",
                "Acute Observation Bay 2",
                "Pediatric Emergency Bay",
                "ER Fast Track 1"
        );

        model.addAttribute("emergencyCase", emergencyCase);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        model.addAttribute("bayPresets", bayPresets);
        model.addAttribute("selectedPatientId", patientId);

        return "emergency/form";
    }

    /**
     * Save Emergency Intake
     */
    @PostMapping("/intake")
    public String saveIntake(@ModelAttribute EmergencyCase emergencyCase,
                             @RequestParam(value = "patientId", required = false) Long patientId,
                             @RequestParam(value = "doctorId", required = false) Long doctorId,
                             RedirectAttributes redirectAttributes) {
        try {
            EmergencyCase saved = emergencyService.registerIntake(emergencyCase, patientId, doctorId);
            redirectAttributes.addFlashAttribute("success",
                    "Emergency Intake Registered: Case #" + saved.getCaseNumber() + " (" + saved.getPatientName() + ") placed in " + saved.getTriageLevelDisplayName());
            return "redirect:/emergency/case/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to register emergency case: " + e.getMessage());
            return "redirect:/emergency/intake";
        }
    }

    /**
     * Live ER Clinical Sheet & Trauma Monitor
     */
    @GetMapping({"/case/{id}", "/{id}"})
    public String viewCase(@PathVariable("id") Long id, Model model) {
        EmergencyCase ec = emergencyService.findById(id);
        List<Bed> availableBeds = bedRepo.findByStatus("Available");
        List<Doctor> doctors = doctorService.findAll();

        model.addAttribute("emergencyCase", ec);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("doctors", doctors);

        return "emergency/view";
    }

    /**
     * Live Vitals & Treatment Intervention Update
     */
    @PostMapping("/case/{id}/update-vitals")
    public String updateVitals(@PathVariable("id") Long id,
                               @RequestParam(value = "bloodPressure", required = false) String bloodPressure,
                               @RequestParam(value = "heartRate", required = false) Integer heartRate,
                               @RequestParam(value = "respiratoryRate", required = false) Integer respiratoryRate,
                               @RequestParam(value = "oxygenSaturation", required = false) Double oxygenSaturation,
                               @RequestParam(value = "temperature", required = false) Double temperature,
                               @RequestParam(value = "glasgowComaScale", required = false) Integer glasgowComaScale,
                               @RequestParam(value = "bloodGlucose", required = false) Integer bloodGlucose,
                               @RequestParam(value = "painScore", required = false) Integer painScore,
                               @RequestParam(value = "treatmentNotes", required = false) String treatmentNotes,
                               @RequestParam(value = "triageNotes", required = false) String triageNotes,
                               RedirectAttributes redirectAttributes) {
        try {
            emergencyService.updateVitals(id, bloodPressure, heartRate, respiratoryRate,
                    oxygenSaturation, temperature, glasgowComaScale, bloodGlucose, painScore,
                    treatmentNotes, triageNotes);
            redirectAttributes.addFlashAttribute("success", "Clinical vitals and treatment intervention updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating vitals: " + e.getMessage());
        }
        return "redirect:/emergency/case/" + id;
    }

    /**
     * Fast ER Status Lifecycle Updater
     */
    @PostMapping("/case/{id}/update-status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "disposition", required = false) String disposition,
                               @RequestParam(value = "dispositionNotes", required = false) String dispositionNotes,
                               @RequestParam(value = "doctorId", required = false) Long doctorId,
                               RedirectAttributes redirectAttributes) {
        try {
            EmergencyCase updated = emergencyService.updateStatus(id, status, disposition, dispositionNotes, doctorId);
            redirectAttributes.addFlashAttribute("success", "Emergency case status updated to: " + updated.getStatusDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating status: " + e.getMessage());
        }
        return "redirect:/emergency/case/" + id;
    }

    /**
     * Direct Admission to Inpatient / ICU Bed
     */
    @PostMapping("/case/{id}/admit")
    public String admitToBed(@PathVariable("id") Long id,
                             @RequestParam("bedId") Long bedId,
                             @RequestParam(value = "admissionReason", required = false) String admissionReason,
                             RedirectAttributes redirectAttributes) {
        try {
            EmergencyCase admitted = emergencyService.admitToBed(id, bedId, admissionReason);
            redirectAttributes.addFlashAttribute("success",
                    "Emergency patient transferred successfully! " + admitted.getDisposition());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Admission failed: " + e.getMessage());
        }
        return "redirect:/emergency/case/" + id;
    }

    /**
     * Delete Emergency Record (with safety check)
     */
    @PostMapping("/case/{id}/delete")
    public String deleteCase(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            emergencyService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Emergency case #" + id + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete record: " + e.getMessage());
        }
        return "redirect:/emergency";
    }
}
