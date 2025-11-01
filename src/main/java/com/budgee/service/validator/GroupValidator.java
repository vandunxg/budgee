package com.budgee.service.validator;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;
import com.budgee.payload.request.group.GroupMemberRequest;

@Component
@Slf4j(topic = "GROUP-VALIDATOR")
public class GroupValidator extends Validator {

    public void validateSingleCreator(List<GroupMemberRequest> requests) {
        log.debug("[validateSingleCreator]");

        AtomicInteger count = new AtomicInteger(0);

        requests.forEach(
                x -> {
                    if (x.isCreator()) {
                        count.getAndIncrement();
                    }
                });

        if (count.get() > 1) {
            throw new ValidationException(ErrorCode.DUPLICATE_CREATOR_ASSIGNMENT);
        }
    }
}
