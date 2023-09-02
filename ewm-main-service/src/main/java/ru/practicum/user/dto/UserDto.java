package ru.practicum.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    @NotBlank(message = "Почта пуста.")
    @Email(message = "Почта не соответствует требованиям.")
    @Size(min = 6, max = 254, message = "Почта от 6 до 254 символов.")
    String email;
    @NotBlank(message = "Имя не указано.")
    @Size(min = 2, max = 250, message = "Имя от 2 до 250 символов.")
    String name;
}
