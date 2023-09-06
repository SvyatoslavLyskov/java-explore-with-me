package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    @NotNull
    @Min(value = -90, message = "Значение широты не менее -90")
    @Max(value = 90, message = "Значение широты не более 90")
    Float lat;
    @NotNull
    @Min(value = -180, message = "Значение долготы не менее -180")
    @Max(value = 180, message = "Значение долготы не более 180")
    Float lon;
}