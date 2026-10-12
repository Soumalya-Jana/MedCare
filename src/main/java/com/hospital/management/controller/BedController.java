package com.hospital.management.controller;

import com.hospital.management.model.Bed;
import com.hospital.management.model.Ward;
import com.hospital.management.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/beds")
public class BedController {

    @Autowired
    private BedService bedService;

    @GetMapping
    public String listBeds(@RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "wardId", required = false) Long wardId,
                           @RequestParam(value = "search", required = false) String search,
                           Model model) {
        List<Bed> allBeds = bedService.findAllBeds();

        long totalCount = allBeds.size();
        long availableCount = allBeds.stream().filter(b -> "Available".equalsIgnoreCase(b.getStatus())).count();
        long occupiedCount = allBeds.stream().filter(b -> "Occupied".equalsIgnoreCase(b.getStatus())).count();
        long maintenanceCount = allBeds.stream().filter(b -> "Maintenance".equalsIgnoreCase(b.getStatus())).count();

        List<Bed> filtered = allBeds;

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            filtered = filtered.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());
        }

        if (wardId != null) {
            filtered = filtered.stream()
                    .filter(b -> b.getWard() != null && wardId.equals(b.getWard().getId()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(b -> (b.getBedNumber() != null && b.getBedNumber().toLowerCase().contains(q)) ||
                                 (b.getWard() != null && b.getWard().getWardName().toLowerCase().contains(q)) ||
                                 (b.getPatient() != null && b.getPatient().getFullName().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("beds", filtered);
        model.addAttribute("wards", bedService.findAllWards());
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("maintenanceCount", maintenanceCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedWardId", wardId);
        model.addAttribute("search", search);

        return "beds/list";
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam(value = "patientId", required = false) Long patientId, Model model) {
        model.addAttribute("bed", new Bed());
        model.addAttribute("wards", bedService.findAllWards());
        model.addAttribute("patients", bedService.getAllPatients());
        model.addAttribute("selectedPatientId", patientId);
        return "beds/form";
    }

    @PostMapping("/save")
    public String saveBed(@ModelAttribute Bed bed,
                          @RequestParam("wardId") Long wardId,
                          @RequestParam(value = "patientId", required = false) Long patientId,
                          RedirectAttributes redirectAttributes) {
        bedService.saveBed(bed, wardId, patientId);
        redirectAttributes.addFlashAttribute("success", "Bed saved successfully!");
        return "redirect:/beds";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("bed", bedService.findBedById(id));
        model.addAttribute("wards", bedService.findAllWards());
        model.addAttribute("patients", bedService.getAllPatients());
        return "beds/form";
    }

    @Autowired
    private com.hospital.management.service.DoctorService doctorService;

    @GetMapping("/discharge/{id}")
    public String showDischargeForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Bed bed = bedService.findBedById(id);
        if (bed.getPatient() == null) {
            redirectAttributes.addFlashAttribute("error", "Bed " + bed.getBedNumber() + " is currently vacant; no patient is admitted.");
            return "redirect:/beds";
        }

        java.time.LocalDateTime admissionDate = bed.getAdmissionDate() != null ? bed.getAdmissionDate() : java.time.LocalDateTime.now();
        java.time.LocalDateTime dischargeDate = java.time.LocalDateTime.now();
        long daysStayed = bedService.calculateDaysStayed(admissionDate, dischargeDate);
        double dailyRate = bed.getDailyRate() != null ? bed.getDailyRate() : 0.0;
        double estimatedRoomCharges = daysStayed * dailyRate;

        model.addAttribute("bed", bed);
        model.addAttribute("patient", bed.getPatient());
        model.addAttribute("admissionDate", admissionDate);
        model.addAttribute("dischargeDate", dischargeDate);
        model.addAttribute("daysStayed", daysStayed);
        model.addAttribute("dailyRate", dailyRate);
        model.addAttribute("estimatedRoomCharges", estimatedRoomCharges);
        model.addAttribute("doctors", doctorService.findAll());

        return "beds/discharge";
    }

    @PostMapping("/discharge/{id}")
    public String processDischarge(@PathVariable("id") Long id,
                                   @RequestParam(value = "dischargeCondition", required = false, defaultValue = "Stable") String dischargeCondition,
                                   @RequestParam(value = "dischargeNotes", required = false) String dischargeNotes,
                                   @RequestParam(value = "followUpAdvice", required = false) String followUpAdvice,
                                   @RequestParam(value = "attendingDoctorName", required = false) String attendingDoctorName,
                                   @RequestParam(value = "generateBill", required = false, defaultValue = "false") boolean generateBill,
                                   @RequestParam(value = "discount", required = false, defaultValue = "0.0") Double discount,
                                   @RequestParam(value = "paymentMethod", required = false, defaultValue = "Cash") String paymentMethod,
                                   @RequestParam(value = "markPaid", required = false, defaultValue = "false") boolean markPaid,
                                   @RequestParam(value = "patientStatus", required = false, defaultValue = "Discharged") String patientStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            com.hospital.management.model.InpatientAdmission admission = bedService.dischargeWithSummary(
                    id, java.time.LocalDateTime.now(), dischargeCondition, dischargeNotes, followUpAdvice,
                    attendingDoctorName, generateBill, discount, paymentMethod, markPaid, patientStatus
            );
            redirectAttributes.addFlashAttribute("success", "Inpatient discharged successfully! Discharge summary & clearance generated.");
            return "redirect:/beds/discharge-summary/" + admission.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error discharging patient: " + ex.getMessage());
            return "redirect:/beds";
        }
    }

    @GetMapping("/discharge-summary/{id}")
    public String viewDischargeSummary(@PathVariable("id") Long id, Model model) {
        com.hospital.management.model.InpatientAdmission admission = bedService.findAdmissionById(id);
        model.addAttribute("admission", admission);
        model.addAttribute("patient", admission.getPatient());
        model.addAttribute("bill", admission.getBill());
        return "beds/discharge-summary";
    }

    @GetMapping("/delete/{id}")
    public String deleteBed(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bedService.deleteBed(id);
            redirectAttributes.addFlashAttribute("success", "Bed deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete bed: It is currently occupied or assigned to an inpatient.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete bed: " + ex.getMessage());
        }
        return "redirect:/beds";
    }

    // ---- Ward Management ----
    @GetMapping("/wards")
    public String listWards(Model model) {
        model.addAttribute("wards", bedService.findAllWards());
        return "beds/wards";
    }

    @GetMapping("/wards/add")
    public String showAddWardForm(Model model) {
        model.addAttribute("ward", new Ward());
        return "beds/ward-form";
    }

    @PostMapping("/wards/save")
    public String saveWard(@ModelAttribute Ward ward, RedirectAttributes redirectAttributes) {
        bedService.saveWard(ward);
        redirectAttributes.addFlashAttribute("success", "Ward saved successfully!");
        return "redirect:/beds/wards";
    }
}
