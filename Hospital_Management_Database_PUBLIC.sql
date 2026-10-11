-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: hospital-management-db-ujjwalsharmasharma09-4636.g.aivencloud.com    Database: defaultdb
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `defaultdb`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `defaultdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `defaultdb`;

--
-- Table structure for table `admissions`
--

DROP TABLE IF EXISTS `admissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admissions` (
  `admission_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `room_id` int NOT NULL,
  `bed_id` int NOT NULL,
  `admission_date` datetime NOT NULL,
  `expected_discharge_date` date DEFAULT NULL,
  `admission_type` enum('REGULAR','EMERGENCY') DEFAULT 'REGULAR',
  `reason` varchar(500) DEFAULT NULL,
  `status` enum('ADMITTED','DISCHARGED') DEFAULT 'ADMITTED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`admission_id`),
  KEY `fk_admission_patient` (`patient_id`),
  KEY `fk_admission_doctor` (`doctor_id`),
  KEY `fk_admission_room` (`room_id`),
  KEY `fk_admission_bed` (`bed_id`),
  CONSTRAINT `fk_admission_bed` FOREIGN KEY (`bed_id`) REFERENCES `beds` (`bed_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_admission_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_admission_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_admission_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `appointment_details`
--

DROP TABLE IF EXISTS `appointment_details`;
/*!50001 DROP VIEW IF EXISTS `appointment_details`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `appointment_details` AS SELECT 
 1 AS `appointment_id`,
 1 AS `appointment_date`,
 1 AS `appointment_time`,
 1 AS `status`,
 1 AS `patient_code`,
 1 AS `patient_name`,
 1 AS `doctor_code`,
 1 AS `doctor_name`,
 1 AS `department_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointments` (
  `appointment_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `department_id` int DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  `notes` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`appointment_id`),
  KEY `fk_appointment_patient` (`patient_id`),
  KEY `fk_appointment_doctor` (`doctor_id`),
  KEY `fk_appointment_department` (`department_id`),
  CONSTRAINT `fk_appointment_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `available_beds`
--

DROP TABLE IF EXISTS `available_beds`;
/*!50001 DROP VIEW IF EXISTS `available_beds`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `available_beds` AS SELECT 
 1 AS `bed_id`,
 1 AS `room_number`,
 1 AS `room_type`,
 1 AS `floor_number`,
 1 AS `bed_number`,
 1 AS `status`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `beds`
--

DROP TABLE IF EXISTS `beds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `beds` (
  `bed_id` int NOT NULL AUTO_INCREMENT,
  `room_id` int NOT NULL,
  `bed_number` varchar(30) NOT NULL,
  `status` enum('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE') DEFAULT 'AVAILABLE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bed_id`),
  UNIQUE KEY `room_id` (`room_id`,`bed_number`),
  CONSTRAINT `fk_bed_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `admission_id` int DEFAULT NULL,
  `bill_date` date NOT NULL,
  `consultation_charge` decimal(10,2) DEFAULT '0.00',
  `room_charge` decimal(10,2) DEFAULT '0.00',
  `medicine_charge` decimal(10,2) DEFAULT '0.00',
  `lab_charge` decimal(10,2) DEFAULT '0.00',
  `other_charge` decimal(10,2) DEFAULT '0.00',
  `subtotal` decimal(10,2) DEFAULT '0.00',
  `discount` decimal(10,2) DEFAULT '0.00',
  `tax` decimal(10,2) DEFAULT '0.00',
  `total_amount` decimal(10,2) DEFAULT '0.00',
  `payment_status` enum('PENDING','PARTIAL','PAID') DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bill_id`),
  KEY `fk_bill_patient` (`patient_id`),
  KEY `fk_bill_admission` (`admission_id`),
  CONSTRAINT `fk_bill_admission` FOREIGN KEY (`admission_id`) REFERENCES `admissions` (`admission_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_bill_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `department_id` int NOT NULL AUTO_INCREMENT,
  `department_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `department_name` (`department_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `discharges`
--

DROP TABLE IF EXISTS `discharges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discharges` (
  `discharge_id` int NOT NULL AUTO_INCREMENT,
  `admission_id` int NOT NULL,
  `discharge_date` datetime NOT NULL,
  `discharge_summary` text,
  `doctor_notes` text,
  `follow_up_instructions` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`discharge_id`),
  UNIQUE KEY `admission_id` (`admission_id`),
  CONSTRAINT `fk_discharge_admission` FOREIGN KEY (`admission_id`) REFERENCES `admissions` (`admission_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `doctor_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `department_id` int DEFAULT NULL,
  `doctor_code` varchar(30) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `specialization` varchar(150) DEFAULT NULL,
  `qualification` varchar(200) DEFAULT NULL,
  `experience_years` int DEFAULT '0',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `consultation_fee` decimal(10,2) DEFAULT '0.00',
  `availability_status` enum('AVAILABLE','UNAVAILABLE') DEFAULT 'AVAILABLE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`doctor_id`),
  UNIQUE KEY `doctor_code` (`doctor_code`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `fk_doctor_department` (`department_id`),
  CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_reports`
--

DROP TABLE IF EXISTS `lab_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `test_id` int NOT NULL,
  `appointment_id` int DEFAULT NULL,
  `request_date` date NOT NULL,
  `report_date` date DEFAULT NULL,
  `result` text,
  `remarks` text,
  `status` enum('PENDING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  KEY `fk_lab_report_patient` (`patient_id`),
  KEY `fk_lab_report_doctor` (`doctor_id`),
  KEY `fk_lab_report_test` (`test_id`),
  KEY `fk_lab_report_appointment` (`appointment_id`),
  CONSTRAINT `fk_lab_report_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_lab_report_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_lab_report_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_lab_report_test` FOREIGN KEY (`test_id`) REFERENCES `lab_tests` (`test_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lab_tests`
--

DROP TABLE IF EXISTS `lab_tests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_tests` (
  `test_id` int NOT NULL AUTO_INCREMENT,
  `test_name` varchar(150) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT '0.00',
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`test_id`),
  UNIQUE KEY `test_name` (`test_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medical_records`
--

DROP TABLE IF EXISTS `medical_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_records` (
  `record_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `appointment_id` int DEFAULT NULL,
  `symptoms` text,
  `diagnosis` text,
  `treatment_notes` text,
  `record_date` date NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  KEY `fk_record_patient` (`patient_id`),
  KEY `fk_record_doctor` (`doctor_id`),
  KEY `fk_record_appointment` (`appointment_id`),
  CONSTRAINT `fk_record_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_record_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_record_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `medicine_stock`
--

DROP TABLE IF EXISTS `medicine_stock`;
/*!50001 DROP VIEW IF EXISTS `medicine_stock`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `medicine_stock` AS SELECT 
 1 AS `medicine_id`,
 1 AS `medicine_name`,
 1 AS `category`,
 1 AS `manufacturer`,
 1 AS `quantity`,
 1 AS `price`,
 1 AS `expiry_date`,
 1 AS `reorder_level`,
 1 AS `stock_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `medicines`
--

DROP TABLE IF EXISTS `medicines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicines` (
  `medicine_id` int NOT NULL AUTO_INCREMENT,
  `medicine_name` varchar(150) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `manufacturer` varchar(150) DEFAULT NULL,
  `quantity` int DEFAULT '0',
  `price` decimal(10,2) DEFAULT '0.00',
  `expiry_date` date DEFAULT NULL,
  `reorder_level` int DEFAULT '10',
  `status` enum('AVAILABLE','OUT_OF_STOCK','EXPIRED') DEFAULT 'AVAILABLE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `patient_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `patient_code` varchar(30) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `blood_group` varchar(10) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `emergency_contact_name` varchar(150) DEFAULT NULL,
  `emergency_contact_phone` varchar(20) DEFAULT NULL,
  `emergency_contact_relation` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`patient_id`),
  UNIQUE KEY `patient_code` (`patient_code`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `bill_id` int NOT NULL,
  `payment_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('CASH','CARD','UPI','NET_BANKING') NOT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `payment_status` enum('SUCCESS','PENDING','FAILED') DEFAULT 'SUCCESS',
  `notes` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `fk_payment_bill` (`bill_id`),
  CONSTRAINT `fk_payment_bill` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prescription_items`
--

DROP TABLE IF EXISTS `prescription_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_items` (
  `prescription_item_id` int NOT NULL AUTO_INCREMENT,
  `prescription_id` int NOT NULL,
  `medicine_id` int NOT NULL,
  `dosage` varchar(100) DEFAULT NULL,
  `frequency` varchar(100) DEFAULT NULL,
  `duration` varchar(100) DEFAULT NULL,
  `instructions` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`prescription_item_id`),
  KEY `fk_prescription_item_prescription` (`prescription_id`),
  KEY `fk_prescription_item_medicine` (`medicine_id`),
  CONSTRAINT `fk_prescription_item_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`medicine_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_item_prescription` FOREIGN KEY (`prescription_id`) REFERENCES `prescriptions` (`prescription_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prescriptions`
--

DROP TABLE IF EXISTS `prescriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescriptions` (
  `prescription_id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `appointment_id` int DEFAULT NULL,
  `prescription_date` date NOT NULL,
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`prescription_id`),
  KEY `fk_prescription_patient` (`patient_id`),
  KEY `fk_prescription_doctor` (`doctor_id`),
  KEY `fk_prescription_appointment` (`appointment_id`),
  CONSTRAINT `fk_prescription_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`doctor_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_prescription_patient` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `room_id` int NOT NULL AUTO_INCREMENT,
  `room_number` varchar(30) NOT NULL,
  `room_type` enum('GENERAL_WARD','SEMI_PRIVATE','PRIVATE','ICU','EMERGENCY') NOT NULL,
  `floor_number` int DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` enum('AVAILABLE','OCCUPIED','MAINTENANCE') DEFAULT 'AVAILABLE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `room_number` (`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `staff_code` varchar(30) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `designation` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `staff_code` (`staff_code`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_staff_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('ADMIN','DOCTOR','STAFF','PATIENT') NOT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Current Database: `defaultdb`
--

USE `defaultdb`;

--
-- Final view structure for view `appointment_details`
--

/*!50001 DROP VIEW IF EXISTS `appointment_details`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`avnadmin`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `appointment_details` AS select `a`.`appointment_id` AS `appointment_id`,`a`.`appointment_date` AS `appointment_date`,`a`.`appointment_time` AS `appointment_time`,`a`.`status` AS `status`,`p`.`patient_code` AS `patient_code`,concat(`p`.`first_name`,' ',coalesce(`p`.`last_name`,'')) AS `patient_name`,`d`.`doctor_code` AS `doctor_code`,concat(`d`.`first_name`,' ',coalesce(`d`.`last_name`,'')) AS `doctor_name`,`dep`.`department_name` AS `department_name` from (((`appointments` `a` join `patients` `p` on((`a`.`patient_id` = `p`.`patient_id`))) join `doctors` `d` on((`a`.`doctor_id` = `d`.`doctor_id`))) left join `departments` `dep` on((`a`.`department_id` = `dep`.`department_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `available_beds`
--

/*!50001 DROP VIEW IF EXISTS `available_beds`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`avnadmin`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `available_beds` AS select `b`.`bed_id` AS `bed_id`,`r`.`room_number` AS `room_number`,`r`.`room_type` AS `room_type`,`r`.`floor_number` AS `floor_number`,`b`.`bed_number` AS `bed_number`,`b`.`status` AS `status` from (`beds` `b` join `rooms` `r` on((`b`.`room_id` = `r`.`room_id`))) where (`b`.`status` = 'AVAILABLE') */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `medicine_stock`
--

/*!50001 DROP VIEW IF EXISTS `medicine_stock`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`avnadmin`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `medicine_stock` AS select `medicines`.`medicine_id` AS `medicine_id`,`medicines`.`medicine_name` AS `medicine_name`,`medicines`.`category` AS `category`,`medicines`.`manufacturer` AS `manufacturer`,`medicines`.`quantity` AS `quantity`,`medicines`.`price` AS `price`,`medicines`.`expiry_date` AS `expiry_date`,`medicines`.`reorder_level` AS `reorder_level`,(case when (`medicines`.`expiry_date` < curdate()) then 'EXPIRED' when (`medicines`.`quantity` <= `medicines`.`reorder_level`) then 'LOW STOCK' else 'AVAILABLE' end) AS `stock_status` from `medicines` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-11 16:16:14
