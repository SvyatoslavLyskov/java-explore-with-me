package ru.practicum.event.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class EventDateValidator implements ConstraintValidator<EventDate, LocalDateTime> {
    @Override
    public boolean isValid(final LocalDateTime valueToValidate, final ConstraintValidatorContext context) {
        return valueToValidate.isAfter(LocalDateTime.now().plusHours(2));
    }
}