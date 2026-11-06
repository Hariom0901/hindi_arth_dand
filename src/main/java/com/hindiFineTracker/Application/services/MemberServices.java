package com.hindiFineTracker.Application.services;
import com.hindiFineTracker.Application.entity.Room;
import com.hindiFineTracker.Application.entity.RoomMember;
import com.hindiFineTracker.Application.repository.RoomMemberRepository;
import com.hindiFineTracker.Application.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
@Service
public class MemberServices {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;

    public  MemberServices(
            RoomRepository roomRepository,
            RoomMemberRepository memberRepository) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public RoomMember joinRoom(String roomCode, String name) {
        String code = roomCode.trim().toUpperCase(Locale.ROOT);
        String cleanName = name.trim();

        if (cleanName.isEmpty() || cleanName.length() > 50) {
            throw new IllegalArgumentException(
                    "Name must contain 1 to 50 characters");
        }

        Room room = roomRepository.findByRoomCode(code)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Room code not found"));

        if (memberRepository.existsByRoom_IdAndNameIgnoreCase(
                room.getId(), cleanName)) {
            throw new IllegalArgumentException(
                    "This name is already in the room");
        }

        return memberRepository.save(
                new RoomMember(room, cleanName));
    }

    @Transactional(readOnly = true)
    public List<RoomMember> getMembers(String roomCode) {
        String code = roomCode.trim().toUpperCase(Locale.ROOT);

        Room room = roomRepository.findByRoomCode(code)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Room code not found"));

        return memberRepository
                .findAllByRoom_IdOrderByJoinedAtAsc(room.getId());
    }
}
