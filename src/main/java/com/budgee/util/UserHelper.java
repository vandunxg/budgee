package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "USER_HELPER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserHelper {}
