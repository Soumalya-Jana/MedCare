package com.hospital.management.repository;

import com.hospital.management.model.InpatientAdmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InpatientAdmissionRepository extends JpaRepository<InpatientAdmission, Long> {

    List<InpatientAdmission> findByPatientIdOrderByDischargeDateDesc(Long patientId);

    List<InpatientAdmission> findByBedIdOrderByDischargeDateDesc(Long bedId);

    List<InpatientAdmission> findAllByOrderByDischargeDateDesc();
}
