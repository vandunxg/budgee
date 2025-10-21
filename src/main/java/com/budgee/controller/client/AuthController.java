package com.budgee.controller.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.AccessDeniedException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budgee.payload.request.LoginRequest;
import com.budgee.payload.request.RegisterRequest;
import com.budgee.service.AuthService;
import com.budgee.service.UserService;
import com.budgee.util.ResponseUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    UserService userService;
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        log.info("[POST auth/register]={}", request.email());

        return ResponseUtil.created(userService.createUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request)
            throws AccessDeniedException {
        log.info("[POST /auth/login]={}", request.email());

        return ResponseUtil.success("Login successfully", authService.getAccessToken(request));
    }
}
