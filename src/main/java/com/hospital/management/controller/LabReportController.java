package com.hospital.management.controller;

import com.hospital.management.model.LabReport;
import com.hospital.management.service.LabReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lab")
public class LabReportController {

    @Autowired
    private LabReportService labReportService;

    @GetMapping
    public String listReports(@RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "search", required = false) String search,
                              Model model) {
        java.util.List<LabReport> reports = labReportService.findAll();

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            reports = reports.stream()
                    .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            reports = reports.stream()
                    .filter(r -> category.equalsIgnoreCase(r.getTestCategory()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.toLowerCase();
            reports = reports.stream()
                    .filter(r -> (r.getTestName() != null && r.getTestName().toLowerCase().contains(q)) ||
                                 (r.getPatient() != null && r.getPatient().getFullName().toLowerCase().contains(q)) ||
                                 (r.getTestCategory() != null && r.getTestCategory().toLowerCase().contains(q)) ||
                                 (r.getAttachmentFileName() != null && r.getAttachmentFileName().toLowerCase().contains(q)))
                    .collect(java.util.stream.Collectors.toList());
        }

        java.util.List<LabReport> allReports = labReportService.findAll();
        double totalRevenue = allReports.stream()
                .mapToDouble(r -> r.getCost() != null ? r.getCost() : 0.0)
                .sum();

        long attachmentCount = allReports.stream()
                .filter(LabReport::hasAttachment)
                .count();

        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", labReportService.countAll());
        model.addAttribute("pendingCount", labReportService.countByStatus("Pending"));
        model.addAttribute("inProgressCount", labReportService.countByStatus("In Progress"));
        model.addAttribute("completedCount", labReportService.countByStatus("Completed"));
        model.addAttribute("attachmentCount", attachmentCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("categories", java.util.List.of("Blood Test", "Urine Test", "Biochemistry", "X-Ray", "CT Scan", "MRI Scan", "Ultrasound", "ECG", "Pathology"));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);

        return "lab/list";
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam(value = "patientId", required = false) Long patientId,
                              @RequestParam(value = "doctorId", required = false) Long doctorId,
                              Model model) {
        model.addAttribute("report", new LabReport());
        model.addAttribute("patients", labReportService.getAllPatients());
        model.addAttribute("doctors", labReportService.getAllDoctors());
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("selectedDoctorId", doctorId);
        return "lab/form";
    }

    @PostMapping("/save")
    public String saveReport(@ModelAttribute LabReport report,
                             @RequestParam("patientId") Long patientId,
                             @RequestParam(value = "doctorId", required = false) Long doctorId,
                             @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                             @RequestParam(value = "removeAttachment", required = false, defaultValue = "false") boolean removeAttachment,
                             RedirectAttributes redirectAttributes) {
        try {
            labReportService.saveWithAttachment(report, patientId, doctorId, attachment, removeAttachment);
            redirectAttributes.addFlashAttribute("success", "Lab report saved successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error saving lab report: " + ex.getMessage());
            return "redirect:/lab";
        }
        return "redirect:/lab";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("report", labReportService.findById(id));
        model.addAttribute("patients", labReportService.getAllPatients());
        model.addAttribute("doctors", labReportService.getAllDoctors());
        return "lab/form";
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public String viewReport(@PathVariable("id") Long id, Model model) {
        model.addAttribute("report", labReportService.findById(id));
        return "lab/view";
    }

    /**
     * Inline preview endpoint for images, PDFs, and diagnostic files.
     */
    @GetMapping("/attachment/{id}")
    @ResponseBody
    public ResponseEntity<Resource> viewAttachmentInline(@PathVariable("id") Long id) {
        LabReport report = labReportService.findById(id);
        if (!report.hasAttachment()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = labReportService.getFileStorageService().loadFileAsResource(report.getAttachmentFilePath());
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = report.getAttachmentFileType();
        if (contentType == null || contentType.isEmpty() || "application/octet-stream".equals(contentType)) {
            contentType = labReportService.getFileStorageService().determineContentType(report.getAttachmentFileName());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + report.getAttachmentFileName() + "\"")
                .body(resource);
    }

    /**
     * Direct download endpoint for diagnostic scan or report attachment.
     */
    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadAttachment(@PathVariable("id") Long id) {
        LabReport report = labReportService.findById(id);
        if (!report.hasAttachment()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = labReportService.getFileStorageService().loadFileAsResource(report.getAttachmentFilePath());
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = report.getAttachmentFileType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = labReportService.getFileStorageService().determineContentType(report.getAttachmentFileName());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getAttachmentFileName() + "\"")
                .body(resource);
    }

    /**
     * Direct one-click scan upload from the report view page or modal.
     */
    @PostMapping("/quick-upload/{id}")
    public String quickUploadAttachment(@PathVariable("id") Long id,
                                        @RequestParam("attachment") MultipartFile attachment,
                                        RedirectAttributes redirectAttributes) {
        try {
            if (attachment == null || attachment.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a diagnostic file or scan image to upload.");
            } else {
                labReportService.updateAttachment(id, attachment);
                redirectAttributes.addFlashAttribute("success", "Medical scan attached successfully!");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload attachment: " + ex.getMessage());
        }
        return "redirect:/lab/view/" + id;
    }

    /**
     * Remove attachment from report.
     */
    @PostMapping("/remove-attachment/{id}")
    public String removeAttachment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            labReportService.removeAttachment(id);
            redirectAttributes.addFlashAttribute("success", "Attachment removed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove attachment: " + ex.getMessage());
        }
        return "redirect:/lab/view/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteReport(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            labReportService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Lab report deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete lab report: It is referenced in patient billing records.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete lab report: " + ex.getMessage());
        }
        return "redirect:/lab";
    }
}
