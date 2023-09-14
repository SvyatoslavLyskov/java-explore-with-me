package ru.practicum.comment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    @NotBlank(message = "Комментарий не должен быть пуст.", groups = {Create.class, Update.class})
    @Size(max = 1000, message = "Допустимое количество символов комментария 1000.",
            groups = {Create.class, Update.class})
    String text;
    UserShortDto author;
    Long eventId;
    LocalDateTime created;
    Boolean edited;
}