package com.budgee.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "CommonHelper")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommonHelper {

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

    public <T> void updateIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        log.info("[updateIfChanged]");

        T oldValue = getter.get();

        if (!Objects.equals(oldValue, newValue)) {
            log.trace("[updateIfChanged] Updated field from {} to {}", oldValue, newValue);

            setter.accept(newValue);
        }
    }
}
