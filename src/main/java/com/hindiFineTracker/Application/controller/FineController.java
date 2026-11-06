package com.hindiFineTracker.Application.controller;
import com.hindiFineTracker.Application.services.FineServices;
import com.hindiFineTracker.Application.services.FineServices.BalanceResponse;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.hindiFineTracker.Application.services.FineServices.HistoryResponse;
@RestController
@RequestMapping("/api/rooms/{roomCode}/members/{memberId}")
public class FineController {

    private final FineServices fineServices;

    public FineController(FineServices fineServices) {
        this.fineServices = fineServices;
    }

    // Record one Hindi-word fine of ₹5
    @PostMapping("/fines")
    public BalanceResponse addFine(
            @PathVariable String roomCode,
            @PathVariable Long memberId) {

        return fineServices.addFine(roomCode, memberId);
    }

    // Record a partial or full payment
    @PostMapping("/payments")
    public BalanceResponse makePayment(
            @PathVariable String roomCode,
            @PathVariable Long memberId,
            @RequestBody PaymentRequest request) {

        return fineServices.makePayment(
                roomCode, memberId, request.amount());
    }

    // View the current balance
    @GetMapping("/balance")
    public BalanceResponse getBalance(
            @PathVariable String roomCode,
            @PathVariable Long memberId) {

        return fineServices.getBalance(roomCode, memberId);
    }

    public record PaymentRequest(long amount) {
    }
    @GetMapping("/history")
    public List<HistoryResponse> getHistory(
            @PathVariable String roomCode,
            @PathVariable Long memberId) {

        return fineServices.getHistory(roomCode, memberId);
    }
}