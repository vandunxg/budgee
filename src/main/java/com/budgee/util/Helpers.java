package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.model.Group;
import com.budgee.model.OwnerEntity;
import com.budgee.model.User;
import com.budgee.service.GroupService;
import com.budgee.service.UserService;

@Component
@Slf4j(topic = "HELPERS")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Helpers {

    UserService userService;
    GroupService groupService;

    public <T extends OwnerEntity> void checkIsOwner(T entity) {
        log.info("[checkIsOwner]");

        User authenticatedUser = userService.getCurrentUser();

        entity.checkIsOwner(authenticatedUser);
    }

    public void checkEndDateBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        log.info("[checkEndDateAfterStartDate] startDate={} endDate={}", startDate, endDate);

        if (!startDate.isBefore(endDate)) {
            throw new ValidationException(ErrorCode.START_DATE_NOT_BEFORE_AFTER_DATE);
        }
    }

    public Group getGroupById(UUID groupId) {
        log.info("[getGroupById]={}", groupId);

        return groupService.getGroupById(groupId);
    }
}
