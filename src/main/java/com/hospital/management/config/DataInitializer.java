package com.hospital.management.config;

import com.hospital.management.model.*;
import com.hospital.management.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Initializes the database with sample data on first run.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRepository userRepo,
            PatientRepository patientRepo,
            DoctorRepository doctorRepo,
            WardRepository wardRepo,
            BedRepository bedRepo,
            MedicineRepository medicineRepo,
            AppointmentRepository appointmentRepo,
            LabReportRepository labReportRepo,
            BillRepository billRepo,
            PrescriptionRepository prescriptionRepo,
            EmergencyCaseRepository emergencyRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Seed prescriptions if missing
            if (prescriptionRepo.count() == 0 && patientRepo.count() > 0 && doctorRepo.count() > 0) {
                seedPrescriptions(prescriptionRepo, patientRepo, doctorRepo);
            }

            // Seed emergency cases if missing
            if (emergencyRepo.count() == 0 && patientRepo.count() > 0) {
                seedEmergencyCases(emergencyRepo, patientRepo, doctorRepo);
            }

            // Only seed if database is empty
            if (userRepo.count() > 0) {
                // Ensure existing lab reports have sample attachments linked
                for (LabReport lr : labReportRepo.findAll()) {
                    if ("Chest X-Ray (PA View)".equalsIgnoreCase(lr.getTestName()) && !lr.hasAttachment()) {
                        lr.setAttachmentFileName("chest_xray_pa_view.jpg");
                        lr.setAttachmentFileType("image/jpeg");
                        lr.setAttachmentFilePath("lab_2_chest_xray_pa_view.jpg");
                        lr.setAttachmentFileSize(645597L);
                        labReportRepo.save(lr);
                    }
                }
                boolean hasMri = labReportRepo.findAll().stream().anyMatch(r -> "MRI Scan".equalsIgnoreCase(r.getTestCategory()));
                if (!hasMri && patientRepo.count() > 0 && doctorRepo.count() > 0) {
                    Patient p1 = patientRepo.findAll().get(0);
                    Doctor doc1 = doctorRepo.findAll().get(0);
                    labReportRepo.save(LabReport.builder()
                            .patient(p1).doctor(doc1)
                            .testName("Brain MRI Scan (Axial T2/FLAIR)")
                            .testCategory("MRI Scan")
                            .result("Cerebral parenchyma demonstrates normal morphology. Normal ventricles and sulci. No intracranial mass effect or acute hemorrhage.")
                            .normalRange("Normal intracranial structure")
                            .remarks("Unremarkable brain MRI scan.")
                            .cost(3500.0)
                            .status("Completed")
                            .attachmentFileName("brain_mri_axial_scan.jpg")
                            .attachmentFileType("image/jpeg")
                            .attachmentFilePath("lab_4_brain_mri_axial.jpg")
                            .attachmentFileSize(602583L)
                            .build());
                }
                return;
            }

            System.out.println("🏥 Seeding database with initial data...");

            // ---- Users ----
            User admin = userRepo.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .email("admin@hospital.com")
                    .phone("9876543210")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());

            User doctorUser1 = userRepo.save(User.builder()
                    .username("dr.sharma")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Dr. Rajesh Sharma")
                    .email("rajesh.sharma@hospital.com")
                    .phone("9876543211")
                    .role(Role.DOCTOR)
                    .enabled(true)
                    .build());

            User doctorUser2 = userRepo.save(User.builder()
                    .username("dr.patel")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Dr. Priya Patel")
                    .email("priya.patel@hospital.com")
                    .phone("9876543212")
                    .role(Role.DOCTOR)
                    .enabled(true)
                    .build());

            User doctorUser3 = userRepo.save(User.builder()
                    .username("dr.khan")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Dr. Amir Khan")
                    .email("amir.khan@hospital.com")
                    .phone("9876543213")
                    .role(Role.DOCTOR)
                    .enabled(true)
                    .build());

            User receptionist = userRepo.save(User.builder()
                    .username("reception")
                    .password(passwordEncoder.encode("reception123"))
                    .fullName("Anita Verma")
                    .email("reception@hospital.com")
                    .phone("9876543214")
                    .role(Role.RECEPTIONIST)
                    .enabled(true)
                    .build());

            // ---- Doctors ----
            Doctor doc1 = doctorRepo.save(Doctor.builder()
                    .fullName("Dr. Rajesh Sharma")
                    .specialization("Cardiology")
                    .qualification("MBBS, MD (Cardiology)")
                    .phone("9876543211")
                    .email("rajesh.sharma@hospital.com")
                    .gender("Male")
                    .consultationFee(800.0)
                    .availableDays("Mon,Tue,Wed,Thu,Fri")
                    .availableTimeStart("09:00")
                    .availableTimeEnd("17:00")
                    .status("Active")
                    .user(doctorUser1)
                    .build());

            Doctor doc2 = doctorRepo.save(Doctor.builder()
                    .fullName("Dr. Priya Patel")
                    .specialization("Pediatrics")
                    .qualification("MBBS, MD (Pediatrics)")
                    .phone("9876543212")
                    .email("priya.patel@hospital.com")
                    .gender("Female")
                    .consultationFee(600.0)
                    .availableDays("Mon,Tue,Wed,Thu,Fri,Sat")
                    .availableTimeStart("10:00")
                    .availableTimeEnd("18:00")
                    .status("Active")
                    .user(doctorUser2)
                    .build());

            Doctor doc3 = doctorRepo.save(Doctor.builder()
                    .fullName("Dr. Amir Khan")
                    .specialization("Orthopedics")
                    .qualification("MBBS, MS (Orthopedics)")
                    .phone("9876543213")
                    .email("amir.khan@hospital.com")
                    .gender("Male")
                    .consultationFee(700.0)
                    .availableDays("Mon,Wed,Fri,Sat")
                    .availableTimeStart("08:00")
                    .availableTimeEnd("16:00")
                    .status("Active")
                    .user(doctorUser3)
                    .build());

            // ---- Patients ----
            Patient p1 = patientRepo.save(Patient.builder()
                    .fullName("Amit Kumar")
                    .dateOfBirth(LocalDate.of(1985, 3, 15))
                    .gender("Male")
                    .phone("9898989898")
                    .email("amit.kumar@email.com")
                    .address("12, MG Road, Kolkata")
                    .bloodGroup("O+")
                    .allergies("None")
                    .medicalHistory("Hypertension")
                    .emergencyContactName("Sunita Kumar")
                    .emergencyContactPhone("9898989899")
                    .status("Active")
                    .build());

            Patient p2 = patientRepo.save(Patient.builder()
                    .fullName("Sneha Roy")
                    .dateOfBirth(LocalDate.of(1990, 7, 22))
                    .gender("Female")
                    .phone("9797979797")
                    .email("sneha.roy@email.com")
                    .address("45, Park Street, Kolkata")
                    .bloodGroup("A+")
                    .allergies("Penicillin")
                    .medicalHistory("Asthma")
                    .emergencyContactName("Rahul Roy")
                    .emergencyContactPhone("9797979798")
                    .status("Active")
                    .build());

            Patient p3 = patientRepo.save(Patient.builder()
                    .fullName("Ravi Gupta")
                    .dateOfBirth(LocalDate.of(1978, 11, 5))
                    .gender("Male")
                    .phone("9696969696")
                    .email("ravi.gupta@email.com")
                    .address("78, Salt Lake, Kolkata")
                    .bloodGroup("B+")
                    .allergies("Sulfa drugs")
                    .medicalHistory("Diabetes Type 2")
                    .emergencyContactName("Meera Gupta")
                    .emergencyContactPhone("9696969697")
                    .status("Active")
                    .build());

            Patient p4 = patientRepo.save(Patient.builder()
                    .fullName("Kavita Singh")
                    .dateOfBirth(LocalDate.of(1995, 1, 10))
                    .gender("Female")
                    .phone("9595959595")
                    .email("kavita.singh@email.com")
                    .address("23, New Town, Kolkata")
                    .bloodGroup("AB+")
                    .status("Active")
                    .build());

            Patient p5 = patientRepo.save(Patient.builder()
                    .fullName("Suresh Das")
                    .dateOfBirth(LocalDate.of(1960, 6, 18))
                    .gender("Male")
                    .phone("9494949494")
                    .email("suresh.das@email.com")
                    .address("90, Howrah, Kolkata")
                    .bloodGroup("O-")
                    .medicalHistory("Heart Disease, High Cholesterol")
                    .emergencyContactName("Lakshmi Das")
                    .emergencyContactPhone("9494949495")
                    .status("Active")
                    .build());

            // ---- Wards ----
            Ward generalWard = wardRepo.save(Ward.builder()
                    .wardName("General Ward A")
                    .wardType("General")
                    .totalBeds(20)
                    .floorNumber(1)
                    .description("General ward on the first floor")
                    .active(true)
                    .build());

            Ward icuWard = wardRepo.save(Ward.builder()
                    .wardName("ICU Ward")
                    .wardType("ICU")
                    .totalBeds(10)
                    .floorNumber(2)
                    .description("Intensive Care Unit")
                    .active(true)
                    .build());

            Ward privateWard = wardRepo.save(Ward.builder()
                    .wardName("Private Ward B")
                    .wardType("Private")
                    .totalBeds(8)
                    .floorNumber(3)
                    .description("Private rooms with attached bathroom")
                    .active(true)
                    .build());

            // ---- Beds ----
            for (int i = 1; i <= 5; i++) {
                bedRepo.save(Bed.builder()
                        .bedNumber("G-" + String.format("%02d", i))
                        .ward(generalWard)
                        .status(i <= 3 ? "Available" : "Occupied")
                        .dailyRate(500.0)
                        .patient(i == 4 ? p1 : i == 5 ? p2 : null)
                        .build());
            }
            for (int i = 1; i <= 3; i++) {
                bedRepo.save(Bed.builder()
                        .bedNumber("ICU-" + String.format("%02d", i))
                        .ward(icuWard)
                        .status(i == 1 ? "Occupied" : "Available")
                        .dailyRate(2000.0)
                        .patient(i == 1 ? p5 : null)
                        .build());
            }
            for (int i = 1; i <= 3; i++) {
                bedRepo.save(Bed.builder()
                        .bedNumber("PVT-" + String.format("%02d", i))
                        .ward(privateWard)
                        .status("Available")
                        .dailyRate(1500.0)
                        .build());
            }

            // ---- Medicines ----
            medicineRepo.save(Medicine.builder()
                    .name("Paracetamol 500mg").genericName("Acetaminophen").category("Tablet")
                    .manufacturer("Cipla").price(5.0).stockQuantity(500).reorderLevel(100)
                    .batchNumber("BAT-001").active(true).build());
            medicineRepo.save(Medicine.builder()
                    .name("Amoxicillin 250mg").genericName("Amoxicillin").category("Capsule")
                    .manufacturer("Sun Pharma").price(12.0).stockQuantity(300).reorderLevel(50)
                    .batchNumber("BAT-002").active(true).build());
            medicineRepo.save(Medicine.builder()
                    .name("Omeprazole 20mg").genericName("Omeprazole").category("Capsule")
                    .manufacturer("Dr. Reddy's").price(8.0).stockQuantity(40).reorderLevel(50)
                    .batchNumber("BAT-003").active(true).build());
            medicineRepo.save(Medicine.builder()
                    .name("Cetirizine 10mg").genericName("Cetirizine").category("Tablet")
                    .manufacturer("Cipla").price(3.0).stockQuantity(600).reorderLevel(100)
                    .batchNumber("BAT-004").active(true).build());
            medicineRepo.save(Medicine.builder()
                    .name("Ibuprofen 400mg").genericName("Ibuprofen").category("Tablet")
                    .manufacturer("Mankind").price(6.0).stockQuantity(20).reorderLevel(50)
                    .batchNumber("BAT-005").active(true).build());
            medicineRepo.save(Medicine.builder()
                    .name("Cough Syrup").genericName("Dextromethorphan").category("Syrup")
                    .manufacturer("Dabur").price(85.0).stockQuantity(150).reorderLevel(30)
                    .batchNumber("BAT-006").active(true).build());

            // ---- Appointments (today) ----
            appointmentRepo.save(Appointment.builder()
                    .patient(p1).doctor(doc1)
                    .appointmentDate(LocalDate.now())
                    .appointmentTime(LocalTime.of(10, 0))
                    .reason("Routine heart checkup")
                    .status("Scheduled").build());
            appointmentRepo.save(Appointment.builder()
                    .patient(p2).doctor(doc2)
                    .appointmentDate(LocalDate.now())
                    .appointmentTime(LocalTime.of(11, 30))
                    .reason("Follow-up for asthma")
                    .status("Scheduled").build());
            appointmentRepo.save(Appointment.builder()
                    .patient(p3).doctor(doc1)
                    .appointmentDate(LocalDate.now())
                    .appointmentTime(LocalTime.of(14, 0))
                    .reason("Blood pressure monitoring")
                    .status("Scheduled").build());
            appointmentRepo.save(Appointment.builder()
                    .patient(p4).doctor(doc3)
                    .appointmentDate(LocalDate.now())
                    .appointmentTime(LocalTime.of(15, 30))
                    .reason("Knee pain consultation")
                    .status("Scheduled").build());

            // ---- Lab Reports ----
            labReportRepo.save(LabReport.builder()
                    .patient(p1).doctor(doc1)
                    .testName("Complete Blood Count (CBC)")
                    .testCategory("Blood Test")
                    .result("Hemoglobin: 14.2 g/dL\nWBC: 6,800 /mcL\nRBC: 4.8 million/mcL\nPlatelets: 250,000 /mcL")
                    .normalRange("Hemoglobin: 13.5 - 17.5 g/dL")
                    .remarks("Normal complete blood profile.")
                    .cost(450.0)
                    .status("Completed")
                    .build());

            labReportRepo.save(LabReport.builder()
                    .patient(p2).doctor(doc2)
                    .testName("Chest X-Ray (PA View)")
                    .testCategory("X-Ray")
                    .result("Bilateral lung fields clear. No evidence of active consolidation or pleural effusion.")
                    .normalRange("Normal lung fields")
                    .remarks("Clear chest radiography.")
                    .cost(800.0)
                    .status("Completed")
                    .build());

            labReportRepo.save(LabReport.builder()
                    .patient(p3).doctor(doc1)
                    .testName("Fasting Blood Sugar (FBS) & HbA1c")
                    .testCategory("Biochemistry")
                    .result("Pending sample analysis")
                    .normalRange("FBS: 70 - 100 mg/dL, HbA1c: < 5.7%")
                    .remarks("Awaiting lab technician review")
                    .cost(600.0)
                    .status("In Progress")
                    .build());

            labReportRepo.save(LabReport.builder()
                    .patient(p5).doctor(doc1)
                    .testName("Lipid Profile & Serum Electrolytes")
                    .testCategory("Blood Test")
                    .normalRange("Cholesterol: < 200 mg/dL")
                    .remarks("Urgent cardiac profile check")
                    .cost(1100.0)
                    .status("Pending")
                    .build());

            // ---- Bills ----
            Bill b1 = Bill.builder()
                    .patient(p1)
                    .consultationFee(800.0)
                    .medicineCost(250.0)
                    .labTestCost(450.0)
                    .roomCharges(1000.0)
                    .otherCharges(0.0)
                    .discount(100.0)
                    .paidAmount(2520.0)
                    .paymentMethod("UPI")
                    .status("Paid")
                    .description("OPD Consultation + Blood Work + 2 Days Observation")
                    .build();
            b1.calculateTotal();
            b1.setPaidAmount(b1.getTotalAmount()); // Full payment
            billRepo.save(b1);

            Bill b2 = Bill.builder()
                    .patient(p2)
                    .consultationFee(600.0)
                    .medicineCost(380.0)
                    .labTestCost(800.0)
                    .roomCharges(0.0)
                    .otherCharges(50.0)
                    .discount(0.0)
                    .paidAmount(1000.0)
                    .paymentMethod("Card")
                    .status("Partial")
                    .description("Pediatric Consult & Chest Radiography")
                    .build();
            b2.calculateTotal();
            billRepo.save(b2);

            Bill b3 = Bill.builder()
                    .patient(p3)
                    .consultationFee(800.0)
                    .medicineCost(120.0)
                    .labTestCost(600.0)
                    .roomCharges(0.0)
                    .otherCharges(0.0)
                    .discount(0.0)
                    .paidAmount(0.0)
                    .paymentMethod("Cash")
                    .status("Pending")
                    .description("Diabetes management checkup")
                    .build();
            b3.calculateTotal();
            billRepo.save(b3);

            System.out.println("✅ Database seeded successfully!");
            System.out.println("📋 Default credentials:");
            System.out.println("   Admin    → admin / admin123");
            System.out.println("   Doctor   → dr.sharma / doctor123");
            System.out.println("   Reception→ reception / reception123");
        };
    }

    private void seedPrescriptions(PrescriptionRepository prescriptionRepo, PatientRepository patientRepo, DoctorRepository doctorRepo) {
        System.out.println("💊 Seeding sample medical prescriptions (Rx)...");
        java.util.List<Patient> patients = patientRepo.findAll();
        java.util.List<Doctor> doctors = doctorRepo.findAll();
        if (patients.isEmpty() || doctors.isEmpty()) return;

        Patient p1 = patients.get(0);
        Doctor doc1 = doctors.get(0);

        Prescription rx1 = Prescription.builder()
                .prescriptionNumber("RX-2026-1001")
                .patient(p1)
                .doctor(doc1)
                .prescriptionDate(LocalDate.now())
                .diagnosis("Type 2 Diabetes Mellitus & Essential Hypertension")
                .symptoms("Mild fatigue, polydipsia, high morning blood sugar levels")
                .vitals("BP: 138/88 mmHg | Pulse: 76 bpm | SpO2: 98% | Temp: 98.4°F")
                .advice("Low carbohydrate diet, 30 mins brisk walking daily. Restrict salt intake.")
                .followUpDate(LocalDate.now().plusDays(30))
                .status("Active")
                .build();

        PrescriptionItem item1 = PrescriptionItem.builder()
                .prescription(rx1)
                .medicineName("Metformin 500mg")
                .dosage("1 Tablet")
                .frequency("1-0-1 (Morning & Night)")
                .timing("After Food")
                .duration("30 Days")
                .quantity(60)
                .instructions("Take with or immediately after meals")
                .build();

        PrescriptionItem item2 = PrescriptionItem.builder()
                .prescription(rx1)
                .medicineName("Telmisartan 40mg")
                .dosage("1 Tablet")
                .frequency("1-0-0 (Morning only)")
                .timing("After Food")
                .duration("30 Days")
                .quantity(30)
                .instructions("Take daily in the morning")
                .build();

        rx1.addItem(item1);
        rx1.addItem(item2);
        prescriptionRepo.save(rx1);

        if (patients.size() > 1 && doctors.size() > 1) {
            Patient p2 = patients.get(1);
            Doctor doc2 = doctors.get(1);

            Prescription rx2 = Prescription.builder()
                    .prescriptionNumber("RX-2026-1002")
                    .patient(p2)
                    .doctor(doc2)
                    .prescriptionDate(LocalDate.now().minusDays(2))
                    .diagnosis("Acute Respiratory Infection & Bronchitis")
                    .symptoms("Frequent dry cough, mild chest congestion, sore throat")
                    .vitals("BP: 120/78 mmHg | Pulse: 80 bpm | SpO2: 99% | Temp: 99.2°F")
                    .advice("Steam inhalation twice daily. Avoid chilled water and cold drinks. Adequate hydration.")
                    .followUpDate(LocalDate.now().plusDays(7))
                    .status("Dispensed")
                    .build();

            PrescriptionItem item3 = PrescriptionItem.builder()
                    .prescription(rx2)
                    .medicineName("Amoxicillin 500mg")
                    .dosage("1 Capsule")
                    .frequency("1-0-1 (Morning & Night)")
                    .timing("After Food")
                    .duration("5 Days")
                    .quantity(10)
                    .instructions("Complete full 5-day course without stopping")
                    .dispensed(true)
                    .build();

            PrescriptionItem item4 = PrescriptionItem.builder()
                    .prescription(rx2)
                    .medicineName("Paracetamol 650mg")
                    .dosage("1 Tablet")
                    .frequency("SOS (As needed)")
                    .timing("After Food")
                    .duration("3 Days")
                    .quantity(6)
                    .instructions("Take only if fever or headache persists")
                    .dispensed(true)
                    .build();

            rx2.addItem(item3);
            rx2.addItem(item4);
            prescriptionRepo.save(rx2);
        }
        System.out.println("✅ Sample prescriptions (Rx) seeded successfully!");
    }

    private void seedEmergencyCases(EmergencyCaseRepository emergencyRepo, PatientRepository patientRepo, DoctorRepository doctorRepo) {
        List<Patient> patients = patientRepo.findAll();
        List<Doctor> doctors = doctorRepo.findAll();

        Doctor traumaDoc = doctors.isEmpty() ? null : doctors.get(0);
        Doctor cardioDoc = doctors.size() > 1 ? doctors.get(1) : traumaDoc;

        // Case 1: Code Red - Unidentified Trauma Polytrauma (Critical Active)
        EmergencyCase ec1 = new EmergencyCase();
        ec1.setCaseNumber("ER-20261011-001");
        ec1.setIsUnidentified(true);
        ec1.setTemporaryIdentifier("Trauma Male ~35y - Road Accident");
        ec1.setPatientName("[UNIDENTIFIED] Trauma Male ~35y");
        ec1.setAge(35);
        ec1.setGender("Male");
        ec1.setContactPhone("Brought by Police Highway Patrol");
        ec1.setTriageLevel("CODE_RED");
        ec1.setTriagePriority(1);
        ec1.setStatus("IN_RESUSCITATION");
        ec1.setArrivalDateTime(java.time.LocalDateTime.now().minusMinutes(25));
        ec1.setArrivalMode("AMBULANCE");
        ec1.setIncidentType("TRAUMA_ACCIDENT");
        ec1.setAssignedDoctor(traumaDoc);
        ec1.setTriageNurse("Nurse Priya Nair (Senior Triage)");
        ec1.setBayNumber("Trauma Bay 1 (Resuscitation)");
        ec1.setBloodPressure("85/50");
        ec1.setHeartRate(128);
        ec1.setRespiratoryRate(30);
        ec1.setOxygenSaturation(88.0);
        ec1.setTemperature(97.4);
        ec1.setGlasgowComaScale(8);
        ec1.setBloodGlucose(110);
        ec1.setPainScore(10);
        ec1.setChiefComplaint("Severe polytrauma following high-speed motor vehicle collision. Unresponsive, open right femoral fracture, severe chest wall contusion.");
        ec1.setTriageNotes("Flail chest segment noted right anterolateral. Markedly hypotensive on arrival. Immediate Code Red trauma team activated.");
        ec1.setEmergencyTreatmentGiven("[23:05] Rapid sequence intubation (7.5 cuffed ETT) with cervical collar maintained.\n[23:08] 2x 14G IV access secured; rapid infusion of O-negative uncrossed blood.\n[23:12] FAST ultrasound scan positive in splenorenal recess.\n[23:18] Orthopedic & Trauma Surgery consult on standby for urgent OT transfer.");
        emergencyRepo.save(ec1);

        // Case 2: Code Red - Acute Inferior STEMI (Critical Active)
        EmergencyCase ec2 = new EmergencyCase();
        ec2.setCaseNumber("ER-20261011-002");
        if (!patients.isEmpty()) {
            ec2.setPatient(patients.get(0));
            ec2.setPatientName(patients.get(0).getFullName());
            ec2.setAge(patients.get(0).getAge());
            ec2.setGender(patients.get(0).getGender());
            ec2.setContactPhone(patients.get(0).getPhone());
        } else {
            ec2.setPatientName("John Doe");
            ec2.setAge(45);
            ec2.setGender("Male");
        }
        ec2.setTriageLevel("CODE_RED");
        ec2.setTriagePriority(1);
        ec2.setStatus("IN_RESUSCITATION");
        ec2.setArrivalDateTime(java.time.LocalDateTime.now().minusMinutes(40));
        ec2.setArrivalMode("AMBULANCE");
        ec2.setIncidentType("CARDIAC_ARREST");
        ec2.setAssignedDoctor(cardioDoc);
        ec2.setTriageNurse("Nurse David Miller");
        ec2.setBayNumber("Trauma Bay 2 (Cardiac)");
        ec2.setBloodPressure("165/102");
        ec2.setHeartRate(115);
        ec2.setRespiratoryRate(24);
        ec2.setOxygenSaturation(93.0);
        ec2.setTemperature(98.6);
        ec2.setGlasgowComaScale(14);
        ec2.setBloodGlucose(145);
        ec2.setPainScore(9);
        ec2.setChiefComplaint("Crushing substernal chest pain radiating to left shoulder and jaw for 45 mins. Diaphoretic, pale, acute distress.");
        ec2.setTriageNotes("Stat 12-lead ECG reveals acute ST elevation in leads II, III, and aVF with reciprocal depression. Primary PCI protocol initiated.");
        ec2.setEmergencyTreatmentGiven("[22:45] High-flow supplemental O2 via non-rebreather mask.\n[22:48] Aspirin 325 mg chewed + Clopidogrel 600 mg loading dose.\n[22:52] IV Nitroglycerin infusion titrated; Morphine 4 mg IV.\n[22:58] Cath lab prepped for emergency coronary intervention.");
        emergencyRepo.save(ec2);

        // Case 3: Code Orange - Severe Acute Asthma / Respiratory Failure
        EmergencyCase ec3 = new EmergencyCase();
        ec3.setCaseNumber("ER-20261011-003");
        if (patients.size() > 1) {
            ec3.setPatient(patients.get(1));
            ec3.setPatientName(patients.get(1).getFullName());
            ec3.setAge(patients.get(1).getAge());
            ec3.setGender(patients.get(1).getGender());
            ec3.setContactPhone(patients.get(1).getPhone());
        } else {
            ec3.setPatientName("Priya Patel");
            ec3.setAge(29);
            ec3.setGender("Female");
        }
        ec3.setTriageLevel("CODE_ORANGE");
        ec3.setTriagePriority(2);
        ec3.setStatus("UNDER_OBSERVATION");
        ec3.setArrivalDateTime(java.time.LocalDateTime.now().minusMinutes(65));
        ec3.setArrivalMode("WALK_IN");
        ec3.setIncidentType("RESPIRATORY_DISTRESS");
        ec3.setAssignedDoctor(traumaDoc);
        ec3.setTriageNurse("Nurse Anita Roy");
        ec3.setBayNumber("Acute Observation Bay 1");
        ec3.setBloodPressure("130/82");
        ec3.setHeartRate(108);
        ec3.setRespiratoryRate(28);
        ec3.setOxygenSaturation(90.5);
        ec3.setTemperature(99.1);
        ec3.setGlasgowComaScale(15);
        ec3.setBloodGlucose(105);
        ec3.setPainScore(5);
        ec3.setChiefComplaint("Acute severe exacerbation of bronchial asthma. Tripod positioning, speaking only 2-word sentences, audible expiratory wheeze.");
        ec3.setTriageNotes("Marked respiratory effort with accessory muscle use. Refractory to salbutamol MDI at home.");
        ec3.setEmergencyTreatmentGiven("[22:15] Continuous nebulization with Duolin (Levosalbutamol + Ipratropium).\n[22:20] IV Hydrocortisone 200 mg administered.\n[22:38] Magnesium Sulfate 2g IV over 20 mins.\n[22:55] SpO2 stabilized to 96% on 3L nasal cannula; wheeze improved.");
        emergencyRepo.save(ec3);

        // Case 4: Code Yellow - Compound Fracture / Fall from Height
        EmergencyCase ec4 = new EmergencyCase();
        ec4.setCaseNumber("ER-20261011-004");
        if (patients.size() > 2) {
            ec4.setPatient(patients.get(2));
            ec4.setPatientName(patients.get(2).getFullName());
            ec4.setAge(patients.get(2).getAge());
            ec4.setGender(patients.get(2).getGender());
            ec4.setContactPhone(patients.get(2).getPhone());
        } else {
            ec4.setPatientName("Robert Smith");
            ec4.setAge(52);
            ec4.setGender("Male");
        }
        ec4.setTriageLevel("CODE_YELLOW");
        ec4.setTriagePriority(3);
        ec4.setStatus("TRIAGED");
        ec4.setArrivalDateTime(java.time.LocalDateTime.now().minusMinutes(90));
        ec4.setArrivalMode("WALK_IN");
        ec4.setIncidentType("TRAUMA_ACCIDENT");
        ec4.setAssignedDoctor(traumaDoc);
        ec4.setTriageNurse("Nurse David Miller");
        ec4.setBayNumber("ER Fast Track 1");
        ec4.setBloodPressure("126/80");
        ec4.setHeartRate(88);
        ec4.setRespiratoryRate(18);
        ec4.setOxygenSaturation(98.0);
        ec4.setTemperature(98.4);
        ec4.setGlasgowComaScale(15);
        ec4.setBloodGlucose(98);
        ec4.setPainScore(8);
        ec4.setChiefComplaint("Right wrist deformity and severe localized tenderness after fall from a 6-foot ladder.");
        ec4.setTriageNotes("Skin intact, radial pulse palpable (2+), capillary refill < 2s. No neurovascular deficit in distal hand.");
        ec4.setEmergencyTreatmentGiven("[21:30] Forearm immobilization with volar splint.\n[21:35] IV Tramadol 50 mg + Ondansetron 4 mg infused.\n[21:40] Stat X-Ray Wrist PA/Lateral ordered.");
        emergencyRepo.save(ec4);

        // Case 5: Code Green - Discharged Stable
        EmergencyCase ec5 = new EmergencyCase();
        ec5.setCaseNumber("ER-20261011-005");
        ec5.setPatientName("Ananya Sen");
        ec5.setAge(24);
        ec5.setGender("Female");
        ec5.setContactPhone("+91 98765 43299");
        ec5.setTriageLevel("CODE_GREEN");
        ec5.setTriagePriority(4);
        ec5.setStatus("DISCHARGED_STABLE");
        ec5.setArrivalDateTime(java.time.LocalDateTime.now().minusHours(3));
        ec5.setArrivalMode("WALK_IN");
        ec5.setIncidentType("GENERAL_ACUTE");
        ec5.setBayNumber("Triage Desk B");
        ec5.setBloodPressure("118/74");
        ec5.setHeartRate(74);
        ec5.setRespiratoryRate(16);
        ec5.setOxygenSaturation(99.0);
        ec5.setTemperature(98.2);
        ec5.setGlasgowComaScale(15);
        ec5.setPainScore(2);
        ec5.setChiefComplaint("Accidental sharp kitchen knife laceration on left index finger with active bleeding.");
        ec5.setDisposition("Discharged Stable");
        ec5.setDispositionNotes("Wound irrigated with sterile saline, repaired with 3x Ethilon 4-0 sutures under local anesthesia (1% Lignocaine). Tetanus toxoid booster administered. Oral antibiotics prescribed.");
        ec5.setDispositionDateTime(java.time.LocalDateTime.now().minusHours(1));
        emergencyRepo.save(ec5);

        System.out.println("🚨 Sample Emergency & Triage cases seeded successfully!");
    }
}
