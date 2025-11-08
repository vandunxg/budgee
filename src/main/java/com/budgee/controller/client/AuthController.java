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

import java.nio.file.AccessDeniedException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgee.payload.request.*;
import com.budgee.payload.response.ErrorResponse;
import com.budgee.payload.response.swagger.RegisterApiResponse;
import com.budgee.payload.response.swagger.TokenApiResponse;
import com.budgee.service.AuthService;
import com.budgee.service.VerificationCodeService;
import com.budgee.util.MessageConstants;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    VerificationCodeService verificationCodeService;
    AuthService authService;

    // -------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account using the provided registration information.",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "User created successfully",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                RegisterApiResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Validation failed",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        log.info("[POST auth/register]={}", request.email());

        return ResponseUtil.created(authService.register(request));
    }

    @Operation(
            summary = "Login and retrieve access token",
            description = "Authenticates the user and returns a JWT access and refresh token.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Login successful",
                        content =
                                @Content(
                                        schema = @Schema(implementation = TokenApiResponse.class))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Invalid credentials",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request)
            throws AccessDeniedException {
        log.info("[POST /auth/login]={}", request.email());

        return ResponseUtil.success(
                MessageConstants.LOGIN_SUCCESS, authService.getAccessToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request)
            throws AccessDeniedException {
        log.info("[POST /auth/logout] refresh token={}", request.refreshToken().substring(1, 15));

        authService.logout(request);

        return ResponseUtil.success(MessageConstants.LOGOUT_SUCCESS);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("x-token") String refreshToken)
            throws AccessDeniedException {
        log.info("[POST /auth/refresh-token] refresh token={}", refreshToken.substring(1, 15));

        return ResponseUtil.success(
                MessageConstants.FETCH_SUCCESS, authService.getRefreshToken(refreshToken));
    }

    @PostMapping("/verification/send")
    public ResponseEntity<?> getVerificationCode(@RequestBody SendVerificationRequest request) {
        log.info("[POST /auth/verification/send]={}", request);

        verificationCodeService.getVerificationCode(request);

        return ResponseUtil.success("Send verification request successfully", null);
    }

    @PostMapping("/verification/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerificationRequest request) {
        log.info("[POST /auth/verification/verify]={}", request);

        return ResponseUtil.success(
                "Verify successfully", verificationCodeService.verifyCode(request));
    }
}
