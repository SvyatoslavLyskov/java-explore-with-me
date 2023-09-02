package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.validation.EventDate;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank(message = "Аннотация должна быть заполнена и без пробелов.", groups = Create.class)
    @Size(min = 20, max = 2000, message = "Символов в аннотации больше 20 и меньше 2000.",
            groups = {Create.class, Update.class})
    String annotation;
    @NotNull(message = "id категории не указан.", groups = Create.class)
    Long category;
    @NotBlank(message = "Описание пустое.", groups = Create.class)
    @Size(min = 20, max = 7000, message = "Символов в описании должно быть больше 20, но меньше 7000.",
            groups = {Create.class, Update.class})
    String description;
    @NotNull(message = "Дата не указана.", groups = Create.class)
    @EventDate(message = "Время события более чем на два часа раньше от настоящего момента.",
            groups = Create.class)
    @Future(message = "Время указано в прошлом.", groups = Update.class)
    @JsonFormat(pattern = DATE_TIME_FORMATTER)
    LocalDateTime eventDate;
    @NotNull(message = "Локация не указана.", groups = Create.class)
    LocationDto location;
    Boolean paid;
    Long participantLimit;
    Boolean requestModeration;
    @NotBlank(message = "Заголовок пустой.", groups = Create.class)
    @Size(min = 3, max = 120, message = "Символов заголовка больше 3 и меньше 120.",
            groups = {Create.class, Update.class})
    String title;
    StateAction stateAction;
}