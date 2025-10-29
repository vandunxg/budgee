package com.budgee.payload.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import com.budgee.payload.response.ApiResponse;
import com.budgee.payload.response.RegisterResponse;

@Schema(description = "API Response containing created user")
public class RegisterApiResponse extends ApiResponse<RegisterResponse> {}
