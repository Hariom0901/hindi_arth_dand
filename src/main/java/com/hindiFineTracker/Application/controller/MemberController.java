package com.hindiFineTracker.Application.controller;
import com.hindiFineTracker.Application.entity.RoomMember;
import com.hindiFineTracker.Application.services.MemberServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
@RestController
@RequestMapping("/api/rooms/{roomCode}/members")
public class MemberController {
    private final MemberServices memberService;

    public MemberController(MemberServices memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse join(
            @PathVariable String roomCode,
            @Valid @RequestBody JoinRequest request) {
        try {
            return toResponse(
                    memberService.joinRoom(roomCode, request.name()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public List<MemberResponse> list(
            @PathVariable String roomCode) {
        try {
            return memberService.getMembers(roomCode)
                    .stream()
                    .map(MemberController::toResponse)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private static MemberResponse toResponse(RoomMember member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getJoinedAt());
    }

    public record JoinRequest(
            @NotBlank @Size(max = 50) String name) {}

    public record MemberResponse(
            Long id, String name, LocalDateTime joinedAt) {}
}
