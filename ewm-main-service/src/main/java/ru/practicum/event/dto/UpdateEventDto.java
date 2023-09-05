package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventDto extends BaseEventDto {
    StateAction stateAction;
    public UpdateEventDto() {
        super();
    }
}