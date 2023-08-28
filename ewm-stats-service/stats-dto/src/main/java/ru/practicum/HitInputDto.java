package ru.practicum;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HitInputDto {
    Long id;
    @Size(max = 64, message = "app не более 64 символов.")
    @NotBlank(message = "app пуст.")
    String app;
    @Size(max = 128, message = "uri не более 128 символов.")
    @NotBlank(message = "uri пуст.")
    String uri;
    @Size(max = 64, message = "ip не более 64 символов.")
    @NotBlank(message = "ip пуст.")
    String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "timestamp пуст.")
    LocalDateTime timestamp;
}