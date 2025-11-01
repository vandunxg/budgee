package com.budgee.payload.response.swagger;

import com.budgee.payload.response.ApiResponse;
import com.budgee.payload.response.RegisterResponse;
import com.budgee.payload.response.WalletResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API Response containing created user")
public class WalletApiResponse extends ApiResponse<WalletResponse> {}
