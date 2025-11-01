package com.budgee.service.lookup;

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
@Slf4j(topic = "GROUP-LOOKUP")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupLookup {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GroupRepository groupRepository;

    public Group getGroupById(UUID groupId) {
        log.debug("[getGroupById]={}", groupId);

        return groupRepository
                .findById(groupId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GROUP_NOT_FOUND));
    }
}
