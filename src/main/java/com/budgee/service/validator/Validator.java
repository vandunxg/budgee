package com.budgee.service.validator;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j(topic = "VALIDATOR")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Validator {

    public <T> void updateIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        log.debug("[updateIfChanged]");

        T oldValue = getter.get();

        if (!Objects.equals(oldValue, newValue)) {
            log.trace("[updateIfChanged] Updated field from {} to {}", oldValue, newValue);

            setter.accept(newValue);
        }
    }
}
