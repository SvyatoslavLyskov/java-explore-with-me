package ru.practicum.event.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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