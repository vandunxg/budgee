package com.budgee.payload.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import com.budgee.payload.response.ApiResponse;

@Schema(description = "API Response containing created user UUID")
public class UserIdApiResponse extends ApiResponse<UUID> {}
