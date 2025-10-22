package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.model.Group;
import com.budgee.service.GroupService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GROUP_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupHelper {
    GroupService groupService;

    public Group getGroupById(UUID groupId) {
        log.info("[getGroupById]={}", groupId);

        return groupService.getGroupById(groupId);
    }
}
