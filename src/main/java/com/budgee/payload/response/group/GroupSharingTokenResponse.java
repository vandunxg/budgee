package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSharingTokenResponse implements Serializable {

    UUID groupId;
    String token;
}
