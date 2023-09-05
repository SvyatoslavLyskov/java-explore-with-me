package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.validation.EventDate;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class BaseEventDto {
    @NotBlank(message = "Аннотация пустая или содержит пробелы.", groups = Create.class)
    @Size(min = 20, max = 2000, message = "Символов аннотации больше 20, но меньше 2000.",
            groups = {Create.class, Update.class})
    String annotation;
    @NotNull(message = "id категории не указан.", groups = Create.class)
    Long category;
    @NotBlank(message = "Описание пустое или содержит пробелы.", groups = Create.class)
    @Size(min = 20, max = 7000, message = "Символов описания больше 20, но меньше 7000.",
            groups = {Create.class, Update.class})
    String description;
    @NotNull(message = "Дата события не указана.", groups = Create.class)
    @EventDate(message = "Время не раньше, чем два часа от текущего момента.",
            groups = Create.class)
    @Future(message = "Время указано в прошлом.", groups = Update.class)
    @JsonFormat(pattern = DATE_TIME_FORMATTER)
    LocalDateTime eventDate;
    @NotNull(message = "Локация не указана.", groups = Create.class)
    LocationDto location;
    Boolean paid;
    @PositiveOrZero(message = "Количество участников не может быть отрицательным.",
            groups = {Create.class, Update.class})
    Long participantLimit;
    Boolean requestModeration;
    @NotBlank(message = "Заголовок не может быть пустым и содержать пробелы.", groups = Create.class)
    @Size(min = 3, max = 120, message = "Символов заголовка больше 3, но меньше 120.",
            groups = {Create.class, Update.class})
    String title;
}