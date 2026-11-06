package com.hindiFineTracker.Application.repository;

import com.hindiFineTracker.Application.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.hindiFineTracker.Application.entity.Payment;
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    List<Payment> findAllByMember_IdOrderByCreatedAtDesc(Long memberId);
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.member.id = :memberId
    """)
    long totalPayments(@Param("memberId") Long memberId);
}
