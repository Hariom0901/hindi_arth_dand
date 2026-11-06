package com.hindiFineTracker.Application.repository;
import com.hindiFineTracker.Application.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember,Long> {
    boolean existsByRoom_IdAndNameIgnoreCase(
            Long roomId, String name);

    List<RoomMember> findAllByRoom_IdOrderByJoinedAtAsc(
            Long roomId);
}
//This repository lets us check whether a name already exists in a room and retrieve the room's member list.