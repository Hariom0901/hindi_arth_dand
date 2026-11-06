package com.hindiFineTracker.Application.repository;
import com.hindiFineTracker.Application.entity.FineTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.hindiFineTracker.Application.entity.FineTransactions;
public interface FineTransactionRepository extends JpaRepository<FineTransactions,Long> {
    List<FineTransactions> findAllByMember_IdOrderByCreatedAtDesc(Long memberId);
    @Query("""
        SELECT COALESCE(SUM(f.amount), 0)
        FROM FineTransactions f
        WHERE f.member.id = :memberId
    """)
    long totalFines(@Param("memberId") Long memberId);
}
