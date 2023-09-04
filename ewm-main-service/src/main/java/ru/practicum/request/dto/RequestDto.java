package ru.practicum.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Status;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestDto {
    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    Status status;
}
