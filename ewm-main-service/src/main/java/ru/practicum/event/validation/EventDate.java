package ru.practicum.event.validation;

import ru.practicum.utils.DateTimeFormat;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = EventDateValidator.class)
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface EventDate {
    String message() default "Неверная дата события. Дата должна быть в допустимом формате и быть в будущем.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String format() default DateTimeFormat.DATE_TIME_FORMATTER;
}