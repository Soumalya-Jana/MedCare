package com.hospital.management.controller;

import com.hospital.management.model.Medicine;
import com.hospital.management.service.PharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @GetMapping
    public String listMedicines(@RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "stockFilter", required = false) String stockFilter,
                                Model model) {
        List<Medicine> allMedicines = pharmacyService.findAll();

        // Calculate system-wide inventory KPI metrics
        long totalMedicines = allMedicines.size();
        long outOfStockCount = allMedicines.stream()
                .filter(m -> m.getStockQuantity() == null || m.getStockQuantity() <= 0)
                .count();
        long lowStockCount = allMedicines.stream()
                .filter(m -> m.getStockQuantity() != null && m.getStockQuantity() > 0 &&
                             m.getReorderLevel() != null && m.getStockQuantity() <= m.getReorderLevel())
                .count();
        long inStockCount = totalMedicines - outOfStockCount - lowStockCount;

        double totalInventoryValue = allMedicines.stream()
                .mapToDouble(m -> (m.getPrice() != null ? m.getPrice() : 0.0) * (m.getStockQuantity() != null ? m.getStockQuantity() : 0))
                .sum();

        long totalUnitsInStock = allMedicines.stream()
                .mapToLong(m -> m.getStockQuantity() != null ? m.getStockQuantity() : 0)
                .sum();

        // Apply filters to active view
        List<Medicine> filtered = allMedicines;

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(m -> (m.getName() != null && m.getName().toLowerCase().contains(q)) ||
                                 (m.getGenericName() != null && m.getGenericName().toLowerCase().contains(q)) ||
                                 (m.getManufacturer() != null && m.getManufacturer().toLowerCase().contains(q)) ||
                                 (m.getBatchNumber() != null && m.getBatchNumber().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            filtered = filtered.stream()
                    .filter(m -> category.equalsIgnoreCase(m.getCategory()))
                    .collect(Collectors.toList());
        }

        if (stockFilter != null && !stockFilter.trim().isEmpty()) {
            if ("OUT".equalsIgnoreCase(stockFilter)) {
                filtered = filtered.stream()
                        .filter(m -> m.getStockQuantity() == null || m.getStockQuantity() <= 0)
                        .collect(Collectors.toList());
            } else if ("LOW".equalsIgnoreCase(stockFilter)) {
                filtered = filtered.stream()
                        .filter(m -> m.getStockQuantity() != null && m.getStockQuantity() > 0 &&
                                     m.getReorderLevel() != null && m.getStockQuantity() <= m.getReorderLevel())
                        .collect(Collectors.toList());
            } else if ("NORMAL".equalsIgnoreCase(stockFilter)) {
                filtered = filtered.stream()
                        .filter(m -> m.getStockQuantity() != null && m.getReorderLevel() != null &&
                                     m.getStockQuantity() > m.getReorderLevel())
                        .collect(Collectors.toList());
            }
        }

        List<String> standardCategories = Arrays.asList(
                "Tablet", "Capsule", "Syrup", "Injection", "Ointment", "Drops", "Inhaler", "IV Fluid"
        );

        model.addAttribute("medicines", filtered);
        model.addAttribute("totalMedicines", totalMedicines);
        model.addAttribute("totalUnitsInStock", totalUnitsInStock);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("inStockCount", inStockCount);
        model.addAttribute("totalInventoryValue", totalInventoryValue);
        model.addAttribute("categories", standardCategories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStockFilter", stockFilter);
        model.addAttribute("search", search);
        model.addAttribute("lowStockMedicines", pharmacyService.findLowStockMedicines());

        return "pharmacy/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "pharmacy/form";
    }

    @PostMapping("/save")
    public String saveMedicine(@ModelAttribute Medicine medicine, RedirectAttributes redirectAttributes) {
        pharmacyService.save(medicine);
        redirectAttributes.addFlashAttribute("success", "Medicine inventory record saved successfully!");
        return "redirect:/pharmacy";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("medicine", pharmacyService.findById(id));
        return "pharmacy/form";
    }

    @PostMapping("/restock/{id}")
    public String restockMedicine(@PathVariable("id") Long id,
                                  @RequestParam("quantity") int quantity,
                                  @RequestParam(value = "batchNumber", required = false) String batchNumber,
                                  RedirectAttributes redirectAttributes) {
        try {
            Medicine med = pharmacyService.restock(id, quantity, batchNumber);
            redirectAttributes.addFlashAttribute("success",
                    "Restocked " + quantity + " units for " + med.getName() + "! Updated stock: " + med.getStockQuantity() + " units.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to restock: " + ex.getMessage());
        }
        return "redirect:/pharmacy";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            pharmacyService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Medicine removed from formulary inventory!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete medicine: It has already been dispensed or prescribed in clinical prescriptions.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete medicine: " + ex.getMessage());
        }
        return "redirect:/pharmacy";
    }
}
