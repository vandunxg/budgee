package com.budgee.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "CODE-GENERATOR")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeGenerator {

    String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    SecureRandom RANDOM = new SecureRandom();

    public String generateShortCode(int length) {

        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return code.toString();
    }

    public String generateGroupInviteToken(int codeLength) {
        return generateShortCode(codeLength);
    }
}
