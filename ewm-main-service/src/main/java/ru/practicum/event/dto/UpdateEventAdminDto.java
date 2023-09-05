package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminDto extends UpdateEventDto {
    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT,
    }
}