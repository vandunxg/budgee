package com.budgee.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.WalletRequest;
import com.budgee.payload.response.ErrorResponse;
import com.budgee.payload.response.swagger.WalletApiResponse;
import com.budgee.service.WalletService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
@Slf4j(topic = "WALLET-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Wallet API", description = "CRUD operations for user wallets")
public class WalletController {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    WalletService walletService;

    // -------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------

    @Operation(
            summary = "Get wallet by ID",
            description = "Fetch wallet details using the given UUID.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Wallet fetched successfully",
                        content =
                                @Content(
                                        schema =
                                                @Schema(implementation = WalletApiResponse.class))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Wallet not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @RequestMapping("/{id}")
    ResponseEntity<?> getWallet(@PathVariable UUID id) {
        log.info("[GET /wallets/{}]", id);

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, walletService.getWallet(id));
    }

    @PostMapping("/")
    ResponseEntity<?> createWallet(@RequestBody @Valid WalletRequest request) {
        log.info("[POST /wallets/] {}", request.toString());

        return ResponseUtil.created(walletService.createWallet(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateWallet(
            @PathVariable UUID id, @RequestBody WalletRequest request) {
        log.info("[PATCH /wallets/{}]={}", id, request.toString());

        return ResponseUtil.success(
                MessageConstants.UPDATE_SUCCESS, walletService.updateWallet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWallet(@PathVariable UUID id) {
        log.info("[DELETE /categories/{}]", id);

        walletService.deleteWallet(id);

        return ResponseUtil.deleted();
    }

    @GetMapping("/list")
    ResponseEntity<?> getAllWallets() {
        log.info("[GET /wallets/list/]");

        return ResponseUtil.success(MessageConstants.FETCH_SUCCESS, walletService.getAllWallets());
    }
}
