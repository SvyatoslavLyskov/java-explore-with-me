package ru.practicum.compilation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationInputDto {
    Set<Long> events = new HashSet<>();
    Boolean pinned;
    @Size(max = 50, message = "Заголовок не более 50 символов.", groups = {Create.class, Update.class})
    @NotBlank(message = "Название подборки не указано.", groups = Create.class)
    String title;
}
