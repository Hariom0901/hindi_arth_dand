package com.hindiFineTracker.Application.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity//tells this class represent database entity
@Table(name="rooms")//specify database table name
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // make postgresql generate room id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 8)//prevent duplicate in databse
    private String roomCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Room() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
