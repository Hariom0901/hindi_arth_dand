package com.hindiFineTracker.Application.services;
import com.hindiFineTracker.Application.entity.FineTransactions;
import com.hindiFineTracker.Application.entity.Payment;
import com.hindiFineTracker.Application.entity.Room;
import com.hindiFineTracker.Application.entity.RoomMember;
import com.hindiFineTracker.Application.repository.FineTransactionRepository;
import com.hindiFineTracker.Application.repository.PaymentRepository;
import com.hindiFineTracker.Application.repository.RoomMemberRepository;
import com.hindiFineTracker.Application.repository.RoomRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
@Service
public class FineServices {

    private static final long FINE_PER_WORD = 5;

    private final FineTransactionRepository fineRepository;
    private final PaymentRepository paymentRepository;
    private final RoomMemberRepository memberRepository;
    private final RoomRepository roomRepository;

    public FineServices(
            FineTransactionRepository fineRepository,
            PaymentRepository paymentRepository,
            RoomMemberRepository memberRepository,
            RoomRepository roomRepository) {
        this.fineRepository = fineRepository;
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public BalanceResponse addFine(String roomCode, Long memberId) {
        RoomMember member = getMemberInRoom(roomCode, memberId);

        // The amount is fixed at ₹5; the client cannot choose it.
        fineRepository.save(new FineTransactions(member));

        return getBalance(member);
    }

    @Transactional
    public BalanceResponse makePayment(
            String roomCode, Long memberId, long amount) {

        if (amount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment must be greater than zero");
        }

        RoomMember member = getMemberInRoom(roomCode, memberId);
        BalanceResponse balance = getBalance(member);

        if (amount > balance.remainingBalance()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment cannot exceed the remaining fine");
        }

        paymentRepository.save(new Payment(member, Math.toIntExact(amount)));

        return getBalance(member);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String roomCode, Long memberId) {
        RoomMember member = getMemberInRoom(roomCode, memberId);
        return getBalance(member);
    }

    private BalanceResponse getBalance(RoomMember member) {
        Long fineTotal = fineRepository.totalFines(member.getId());
        Long paymentTotal = paymentRepository.totalPayments(member.getId());

        long fines = fineTotal == null ? 0 : fineTotal;
        long payments = paymentTotal == null ? 0 : paymentTotal;
        long remaining = fines - payments;

        return new BalanceResponse(
                member.getId(),
                member.getName(),
                fines,
                payments,
                remaining
        );
    }

    private RoomMember getMemberInRoom(String roomCode, Long memberId) {
        Room room = roomRepository.findByRoomCode(
                        roomCode.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room not found"));

        RoomMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Member not found"));

        if (!room.getId().equals(member.getRoom().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member does not belong to this room");
        }

        return member;
    }

    public record BalanceResponse(
            Long memberId,
            String memberName,
            long totalFines,
            long totalPayments,
            long remainingBalance) {
    }
    @Transactional(readOnly = true)
    public List<HistoryResponse> getHistory(String roomCode, Long memberId) {
        RoomMember member = getMemberInRoom(roomCode, memberId);

        List<HistoryResponse> history = new ArrayList<>();

        fineRepository
                .findAllByMember_IdOrderByCreatedAtDesc(member.getId())
                .forEach(fine -> history.add(
                        new HistoryResponse(
                                fine.getId(),
                                "FINE",
                                fine.getAmount(),
                                fine.getCreatedAt()
                        )
                ));

        paymentRepository
                .findAllByMember_IdOrderByCreatedAtDesc(member.getId())
                .forEach(payment -> history.add(
                        new HistoryResponse(
                                payment.getId(),
                                "PAYMENT",
                                payment.getAmount(),
                                payment.getCreatedAt()
                        )
                ));

        history.sort(
                Comparator.comparing(HistoryResponse::createdAt).reversed()
        );

        return history;
    }

    public record HistoryResponse(
            Long id,
            String type,
            int amount,
            LocalDateTime createdAt
    ) {
    }
}
//addFine() creates one permanent ₹5 fine record.
//
//
//
//makePayment() allows partial payments but rejects zero, negative, or excessive payments.
//
//
//
//getBalance() calculates the balance from the fine and payment histories.
//
//
//
//getMemberInRoom() prevents someone from using a member ID from a different room