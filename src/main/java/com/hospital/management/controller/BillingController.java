package com.hospital.management.controller;

import com.hospital.management.model.Bill;
import com.hospital.management.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping
    public String listBills(@RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "search", required = false) String search,
                            Model model) {
        List<Bill> allBills = billingService.findAll();

        long totalInvoicesCount = allBills.size();
        long paidInvoicesCount = allBills.stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getStatus()))
                .count();
        long pendingInvoicesCount = allBills.stream()
                .filter(b -> "Pending".equalsIgnoreCase(b.getStatus()))
                .count();
        long partialInvoicesCount = allBills.stream()
                .filter(b -> "Partial".equalsIgnoreCase(b.getStatus()))
                .count();

        double totalRevenue = billingService.getTotalRevenue();
        double totalCollected = billingService.getTotalCollected();
        double totalPending = billingService.getTotalPending();

        List<Bill> filtered = allBills;

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            filtered = filtered.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(b -> (b.getId() != null && ("inv-" + b.getId()).toLowerCase().contains(q)) ||
                                 (b.getPatient() != null && b.getPatient().getFullName().toLowerCase().contains(q)) ||
                                 (b.getPatient() != null && b.getPatient().getPatientId().toLowerCase().contains(q)) ||
                                 (b.getPaymentMethod() != null && b.getPaymentMethod().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("bills", filtered);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("totalInvoicesCount", totalInvoicesCount);
        model.addAttribute("paidInvoicesCount", paidInvoicesCount);
        model.addAttribute("pendingInvoicesCount", pendingInvoicesCount);
        model.addAttribute("partialInvoicesCount", partialInvoicesCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("totalPending", totalPending);

        return "billing/list";
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam(value = "patientId", required = false) Long patientId, Model model) {
        model.addAttribute("bill", new Bill());
        model.addAttribute("patients", billingService.getAllPatients());
        model.addAttribute("selectedPatientId", patientId);
        return "billing/form";
    }

    @PostMapping("/save")
    public String saveBill(@ModelAttribute Bill bill,
                           @RequestParam("patientId") Long patientId,
                           RedirectAttributes redirectAttributes) {
        billingService.save(bill, patientId);
        redirectAttributes.addFlashAttribute("success", "Bill saved successfully!");
        return "redirect:/billing";
    }

    @PostMapping("/pay/{id}")
    public String recordPayment(@PathVariable("id") Long id,
                                @RequestParam("amount") Double amount,
                                @RequestParam("paymentMethod") String paymentMethod,
                                RedirectAttributes redirectAttributes) {
        billingService.recordPayment(id, amount, paymentMethod);
        redirectAttributes.addFlashAttribute("success", "Payment of ₹" + String.format("%.2f", amount) + " recorded successfully!");
        return "redirect:/billing/view/" + id;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("bill", billingService.findById(id));
        model.addAttribute("patients", billingService.getAllPatients());
        return "billing/form";
    }

    @GetMapping({"/view/{id}", "/{id}"})
    public String viewBill(@PathVariable("id") Long id, Model model) {
        model.addAttribute("bill", billingService.findById(id));
        return "billing/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteBill(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            billingService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Bill deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete bill: Linked payments or invoice records exist.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete bill: " + ex.getMessage());
        }
        return "redirect:/billing";
    }
}
