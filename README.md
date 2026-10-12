# 🏥 MedCare — Hospital Management System (HMS)

A full-stack, enterprise-grade Hospital Management System built with **Spring Boot 3.2**, **Thymeleaf**, **Spring Security 6**, **Spring Data JPA (Hibernate)**, and **MySQL (via XAMPP)**. Designed with a dark glassmorphic UI.

---

## 🚀 Key Modules Included

1. **Executive Dashboard & Interactive Analytics**: Real-time KPI statistics, live Chart.js visual charts (monthly revenue trends, inpatient bed occupancy doughnut, appointment pipeline, ward capacity breakdown), and REST analytics API (`/api/analytics/summary`).
2. **Patient Registration & Records**: Complete patient demographic tracking, blood group, medical history, allergies, emergency contacts, and active status.
3. **Doctor Management**: Specialist doctor directory, department mapping, consultation fees, weekly availability schedule, and doctor profiles.
4. **Appointment Scheduling**: Real-time appointment booking between patients and doctors with status workflows (`Scheduled`, `Completed`, `Cancelled`).
5. **Bed & Ward Management**: Inpatient ward capacities (General, ICU, CCU, Private suites), real-time bed occupancy tracking, admissions, and one-click patient discharge.
6. **Pharmacy & Medicine Inventory**: Pharmaceutical stock tracking, batch numbers, manufacturer info, pricing, reorder levels, automatic low-stock alerts, and 1-click quick-restock modal with preset inward quantities (`+10`, `+50`, `+100`).
7. **Laboratory, Diagnostics & Medical Imaging**: Clinical lab test orders, category tracking (Blood, X-Ray, CT, MRI, Pathology), diagnostic readings, reference ranges, print-ready reports, and digital scan file attachments (Chest X-Rays, Brain MRIs, Pathology PDFs) with darkroom radiography viewer and inline document streaming.
8. **Digital Prescriptions (Rx) & E-Prescribing**: Doctor prescription drafting with dosage schedule, frequency, duration, clinical vitals, print-ready Rx slip, 1-click pharmacy dispensing, and automatic billing integration.
9. **Billing & Invoicing**: Automated invoice calculation (consultations, medications, lab tests, room charges, discounts, taxes), partial/full payment tracking, and printable receipts.
10. **Role-Based Security**: Spring Security with BCrypt password hashing, session management, and role-based access control.
11. **Emergency, Trauma & Triage Command Center**: 24/7 acute trauma intake, 5-level Manchester Triage protocol (`Code Red`, `Code Orange`, `Code Yellow`, `Code Green`, `Code Blue`), unidentified walk-in/unconscious trauma patient intake, live biometric telemetry monitoring (BP, Heart Rate, SpO2, Respiratory Rate, Temp, Glasgow Coma Scale, Blood Sugar, Pain Score), animated Lead II continuous ECG rhythm monitor, quick-resuscitation intervention logging, seamless 1-click ICU/ward bed transfer, and real-time executive dashboard emergency broadcast ribbon.

---

## 🔑 Demo Login Accounts

The system automatically initializes and seeds demo data on the first run:

| Role | Username | Password | Dedicated Login Portal | Dedicated Registration Portal | Access Level |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `admin123` | `/login/admin` | `/register/admin` | Full hospital administration & modules |
| **Consulting Doctor** | `dr.sharma` | `doctor123` | `/login/doctor` | `/register/doctor` | Patient care, clinical diagnosis, prescriptions, labs |
| **Receptionist** | `reception` | `reception123` | `/login/receptionist` | `/register/receptionist` | Patient check-in, appointments, bed admissions, billing |

> **Unified Auth Hub**: `/login` and `/register` provide multi-card navigation to jump directly to any role portal.

---

## ⚙️ Tech Stack & Prerequisites

- **Java JDK**: JDK 17+ (Configured for `D:\JDK 25` or system default)
- **Build Tool**: Apache Maven 3.9+ or included Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Database**: MySQL 8 / MariaDB (via XAMPP or standalone MySQL)
- **Web Server**: Embedded Tomcat on port `8080`
- **UI & Styling**: Thymeleaf HTML5, Vanilla CSS3 with glassmorphic cards and animated gradients, Chart.js, Boxicons

---

## 🛠️ How to Run

### Step 1: Clone the Repository
```bash
git clone https://github.com/ujjwalsharma52/Hospital-Management-System.git
cd Hospital-Management-System
```

### Step 2: Ensure MySQL is running
Open XAMPP Control Panel and ensure the **MySQL** module is started (Default port: `3306`, user: `root`, no password).

The database `hospital_db` will automatically have all tables created and seeded by Spring Boot (`DataInitializer.java`).

### Step 3: Start the Application
Simply double-click:
```bat
run.bat
```
Or run using the Maven Wrapper:
- **Windows (Command Prompt / PowerShell)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux**:
  ```bash
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

### Step 4: Open in Browser
Visit:
```
http://localhost:8080
```
Log in using one of the demo credentials above or register a new account through the dedicated registration portals.
