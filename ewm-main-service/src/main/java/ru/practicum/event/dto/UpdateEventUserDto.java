package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserDto extends UpdateEventDto {
    public enum StateAction {
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}