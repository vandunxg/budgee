package com.budgee.service.validator;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.payload.request.group.GroupMemberRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j(topic = "GROUP-VALIDATOR")
public class GroupValidator extends Validator{

    public void ensureSingleCreator(List<GroupMemberRequest> requests) {
        long creatorCount = requests.stream().filter(GroupMemberRequest::isCreator).count();
        if (creatorCount > 1)
            throw new ValidationException(ErrorCode.DUPLICATE_CREATOR_ASSIGNMENT);
    }
}
