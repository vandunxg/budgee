package com.budgee.payload.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    @Builder.Default boolean success = true;

    @Builder.Default LocalDateTime timestamp = LocalDateTime.now();

    String message;
    T data;
    String path;
}
