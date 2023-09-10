package ru.practicum.event.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class UpdateEventDto extends BaseEventDto {
    StateAction stateAction;

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT,
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}