package com.budgee.service;

import java.util.UUID;

import com.budgee.payload.request.RegisterRequest;
import com.budgee.payload.response.RegisterResponse;

public interface UserService {

    RegisterResponse createUser(RegisterRequest request);

    void activateUser(UUID userid);
}
