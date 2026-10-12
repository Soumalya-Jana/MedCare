package com.hospital.management.repository;

import com.hospital.management.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientId(Long patientId);
    List<Bill> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<Bill> findByStatus(String status);
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Bill b")
    Double getTotalCollected();

    @Query("SELECT COALESCE(SUM(b.totalAmount - b.paidAmount), 0) FROM Bill b WHERE b.status != 'Paid'")
    Double getTotalPending();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE MONTH(b.createdAt) = MONTH(CURRENT_DATE) AND YEAR(b.createdAt) = YEAR(CURRENT_DATE)")
    Double getMonthlyRevenue();
}
