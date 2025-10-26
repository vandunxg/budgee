package com.budgee.payload.response.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

import com.budgee.enums.GroupSharingStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupSharingResponse implements Serializable {

    UUID groupId;
    GroupSharingStatus status;
}
