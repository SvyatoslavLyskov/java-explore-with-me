package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestStatusUpdateRequestDto {
    List<Long> requestIds;
    Status status;

    public enum Status {
        CONFIRMED,
        REJECTED
    }
}