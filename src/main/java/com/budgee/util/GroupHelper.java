package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.NotFoundException;
import com.budgee.model.Group;
import com.budgee.repository.GroupRepository;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupHelper {
    GroupRepository groupRepository;

    public Group getGroupById(UUID groupId) {
        log.info("[getGroupById]={}", groupId);

        return groupRepository
                .findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_NOT_FOUND));
    }
}
