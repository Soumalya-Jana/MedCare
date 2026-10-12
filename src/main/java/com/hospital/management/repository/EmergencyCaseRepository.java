package com.hospital.management.repository;

import com.hospital.management.model.EmergencyCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyCaseRepository extends JpaRepository<EmergencyCase, Long> {

    Optional<EmergencyCase> findByCaseNumber(String caseNumber);

    List<EmergencyCase> findByOrderByArrivalDateTimeDesc();

    List<EmergencyCase> findByStatus(String status);

    List<EmergencyCase> findByTriageLevel(String triageLevel);

    List<EmergencyCase> findByStatusInOrderByTriagePriorityAscArrivalDateTimeAsc(List<String> statuses);

    List<EmergencyCase> findByPatientIdOrderByArrivalDateTimeDesc(Long patientId);

    long countByStatus(String status);

    long countByTriageLevel(String triageLevel);

    long countByStatusIn(List<String> statuses);

    @Query("SELECT COUNT(e) FROM EmergencyCase e WHERE e.status IN ('TRIAGED', 'IN_RESUSCITATION', 'UNDER_OBSERVATION')")
    long countActiveCases();

    @Query("SELECT COUNT(e) FROM EmergencyCase e WHERE e.triageLevel = 'CODE_RED' AND e.status IN ('TRIAGED', 'IN_RESUSCITATION', 'UNDER_OBSERVATION')")
    long countActiveCodeRedCases();

    @Query("SELECT COUNT(e) FROM EmergencyCase e WHERE e.triageLevel = 'CODE_ORANGE' AND e.status IN ('TRIAGED', 'IN_RESUSCITATION', 'UNDER_OBSERVATION')")
    long countActiveCodeOrangeCases();
}
