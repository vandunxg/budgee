package com.budgee.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.budgee.exception.ErrorCode;
import com.budgee.exception.ValidationException;

@Component
@Slf4j(topic = "DATE_VALIDATOR")
public class DateValidator {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // SERVICE
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // PUBLIC FUNCTION
    // -------------------------------------------------------------------

    public void checkEndDateBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        log.info("[checkEndDateBeforeStartDate] startDate={} endDate={}", startDate, endDate);

        if (!startDate.isBefore(endDate)) {
            throw new ValidationException(ErrorCode.START_DATE_NOT_BEFORE_AFTER_DATE);
        }
    }

    // -------------------------------------------------------------------
    // PRIVATE FUNCTION
    // -------------------------------------------------------------------

}
