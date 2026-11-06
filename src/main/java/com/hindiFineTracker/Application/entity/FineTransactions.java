package com.hindiFineTracker.Application.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine_transaction")
public class FineTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private RoomMember member;

    @Column(nullable = false)
    private int amount = 5;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FineTransactions() {
    }

    public FineTransactions(RoomMember member) {
        this.member = member;
        this.amount = 5;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public RoomMember getMember() {
        return member;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
