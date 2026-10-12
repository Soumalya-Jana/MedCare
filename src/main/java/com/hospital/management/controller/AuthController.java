package com.hospital.management.controller;

import com.hospital.management.dto.AdminRegisterDto;
import com.hospital.management.dto.DoctorRegisterDto;
import com.hospital.management.dto.ReceptionistRegisterDto;
import com.hospital.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    public static final List<String> SPECIALIZATIONS = Arrays.asList(
            "Cardiology",
            "Pediatrics",
            "Orthopedics",
            "Neurology",
            "General Medicine",
            "Dermatology",
            "Oncology",
            "Radiology",
            "Emergency Medicine",
            "General Surgery",
            "ENT (Otolaryngology)",
            "Gynecology & Obstetrics"
    );

    // ==========================================
    // LOGIN PORTALS
    // ==========================================

    /**
     * Central Login Hub & Universal Login Portal.
     */
    @GetMapping("/login")
    public String loginHub(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("pageTitle", "MedCare Hospital Portals | Sign In");
        return "auth/login-hub";
    }

    /**
     * Dedicated Administrator Login Portal.
     */
    @GetMapping("/login/admin")
    public String loginAdmin(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("portalRole", "ADMIN");
        model.addAttribute("pageTitle", "System Administrator Portal | MedCare");
        return "auth/login-admin";
    }

    /**
     * Dedicated Doctor / Clinical Staff Login Portal.
     */
    @GetMapping("/login/doctor")
    public String loginDoctor(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("portalRole", "DOCTOR");
        model.addAttribute("pageTitle", "Doctor Clinical Portal | MedCare");
        return "auth/login-doctor";
    }

    /**
     * Dedicated Receptionist / Front Desk Login Portal.
     */
    @GetMapping("/login/receptionist")
    public String loginReceptionist(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("portalRole", "RECEPTIONIST");
        model.addAttribute("pageTitle", "Front Desk & Reception Portal | MedCare");
        return "auth/login-receptionist";
    }

    // ==========================================
    // REGISTRATION PORTALS
    // ==========================================

    /**
     * Central Role Selection Gateway for new registrations.
     */
    @GetMapping("/register")
    public String registerGateway(Model model) {
        model.addAttribute("pageTitle", "Join MedCare | Select Portal");
        return "auth/register-hub";
    }

    /**
     * Administrator Registration Form.
     */
    @GetMapping("/register/admin")
    public String showAdminRegisterForm(Model model) {
        if (!model.containsAttribute("adminDto")) {
            model.addAttribute("adminDto", new AdminRegisterDto());
        }
        model.addAttribute("pageTitle", "Register System Administrator | MedCare");
        return "auth/register-admin";
    }

    /**
     * Process Administrator Registration.
     */
    @PostMapping("/register/admin")
    public String processAdminRegister(@ModelAttribute AdminRegisterDto adminDto,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.registerAdmin(adminDto);
            redirectAttributes.addFlashAttribute("success",
                    "Administrator account @" + adminDto.getUsername() + " registered successfully! You may now sign in.");
            return "redirect:/login/admin?registered=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("adminDto", adminDto);
            return "redirect:/register/admin";
        }
    }

    /**
     * Doctor Clinical Registration Form.
     */
    @GetMapping("/register/doctor")
    public String showDoctorRegisterForm(Model model) {
        if (!model.containsAttribute("doctorDto")) {
            model.addAttribute("doctorDto", new DoctorRegisterDto());
        }
        model.addAttribute("specializations", SPECIALIZATIONS);
        model.addAttribute("pageTitle", "Register Consulting Doctor | MedCare");
        return "auth/register-doctor";
    }

    /**
     * Process Doctor Registration.
     */
    @PostMapping("/register/doctor")
    public String processDoctorRegister(@ModelAttribute DoctorRegisterDto doctorDto,
                                        RedirectAttributes redirectAttributes) {
        try {
            userService.registerDoctor(doctorDto);
            redirectAttributes.addFlashAttribute("success",
                    "Doctor profile for " + doctorDto.getFullName() + " created successfully! You may now sign in to your Doctor Portal.");
            return "redirect:/login/doctor?registered=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("doctorDto", doctorDto);
            return "redirect:/register/doctor";
        }
    }

    /**
     * Receptionist Registration Form.
     */
    @GetMapping("/register/receptionist")
    public String showReceptionistRegisterForm(Model model) {
        if (!model.containsAttribute("receptionistDto")) {
            model.addAttribute("receptionistDto", new ReceptionistRegisterDto());
        }
        model.addAttribute("pageTitle", "Register Front Desk Staff | MedCare");
        return "auth/register-receptionist";
    }

    /**
     * Process Receptionist Registration.
     */
    @PostMapping("/register/receptionist")
    public String processReceptionistRegister(@ModelAttribute ReceptionistRegisterDto receptionistDto,
                                             RedirectAttributes redirectAttributes) {
        try {
            userService.registerReceptionist(receptionistDto);
            redirectAttributes.addFlashAttribute("success",
                    "Front Desk account for " + receptionistDto.getFullName() + " registered successfully! You may now sign in.");
            return "redirect:/login/receptionist?registered=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("receptionistDto", receptionistDto);
            return "redirect:/register/receptionist";
        }
    }

    // ==========================================
    // ACCESS DENIED
    // ==========================================

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
