package com.budgee.service;

import java.util.Map;
import java.util.UUID;

import com.budgee.model.User;
import com.budgee.payload.request.RegisterRequest;

public interface UserService {

    Map<String, UUID> createUser(RegisterRequest request);

    User getCurrentUser();
}
