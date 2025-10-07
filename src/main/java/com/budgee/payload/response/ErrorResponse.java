package com.budgee.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorResponse {

    @Builder.Default
    boolean success = false;

    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now();

    int status;
    int code;
    String error;
    String message;
    String path;

    Map<String, String> errors;
}
