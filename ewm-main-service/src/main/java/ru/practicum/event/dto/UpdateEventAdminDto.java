package ru.practicum.event.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminDto extends UpdateEventDto {
    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT,
    }
}