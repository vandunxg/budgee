package com.budgee.service;

import com.budgee.payload.request.RegisterRequest;

import java.util.UUID;

public interface UserService {

    UUID createUser (RegisterRequest request);
}
