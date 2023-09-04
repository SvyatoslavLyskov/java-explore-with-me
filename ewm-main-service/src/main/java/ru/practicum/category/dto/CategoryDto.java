package ru.practicum.category.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDto {
    Long id;
    @Size(max = 50, message = "Название не более 50 символов.")
    @NotBlank(message = "Название категории не указано.")
    String name;
}
