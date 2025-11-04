package com.budgee.service;

import com.budgee.payload.request.RegisterRequest;
import com.budgee.payload.response.RegisterResponse;

public interface UserService {

    RegisterResponse createUser(RegisterRequest request);
}
