package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.TransactionRequest;
import com.budgee.service.TransactionService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {

    TransactionService transactionService;

    @PostMapping("/")
    ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionRequest request) {
        log.info("[POST /transactions/] {}", request.toString());

        return ResponseUtil.created(transactionService.createTransaction(request));
    }

    @PatchMapping("/{id}")
    ResponseEntity<?> updateTransaction(
            @PathVariable UUID id, @RequestBody @Valid TransactionRequest request) {
        log.info("[PATCH /transactions/{}]={}", id, request.toString());

        return ResponseUtil.success(
                "Updated successfully", transactionService.updateTransaction(id, request));
    }

    @RequestMapping("/{id}")
    ResponseEntity<?> getTransaction(@PathVariable UUID id) {
        log.info("[GET /transactions/{}]", id);

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS, transactionService.getTransaction(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable UUID id) {
        log.info("[DELETE /transactions/{}]", id);

        transactionService.deleteTransaction(id);

        return ResponseUtil.deleted();
    }
}
