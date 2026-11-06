package com.hindiFineTracker.Application.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Table(
        name = "room_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_member_name",
                        columnNames = {"room_id", "name"}
                )
        }
)
@Entity
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public RoomMember() {
    }

    public RoomMember(Room room, String name) {
        this.room = room;
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
//@ManyToOne ->means many members can belong to one room.
//
//
//
//@JoinColumn ->stores the room's ID as a foreign key.
//
//
//
//@UniqueConstraint-> prevents the same member name from being added twice to the same room.
//
//
//
//@PrePersist-> records when a member joins.