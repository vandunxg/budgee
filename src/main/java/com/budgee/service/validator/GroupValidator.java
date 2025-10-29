package com.budgee.service.validator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.payload.request.group.GroupMemberRequest;

@Component("groupValidatorNew")
@RequiredArgsConstructor
@Slf4j(topic = "GROUP-VALIDATOR")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupValidator extends Validator {

    public void ensureSingleCreator(List<GroupMemberRequest> requests) {
        long creatorCount = requests.stream().filter(GroupMemberRequest::isCreator).count();
        if (creatorCount > 1) throw new ValidationException(ErrorCode.DUPLICATE_CREATOR_ASSIGNMENT);
    }
}
