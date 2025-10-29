package com.budgee.payload.response.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import com.budgee.payload.response.ApiResponse;
import com.budgee.payload.response.TokenResponse;

@Schema(description = "API Response containing TokenResponse as payload")
public class TokenApiResponse extends ApiResponse<TokenResponse> {}
