package ru.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hits")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hit_id")
    Long id;
    @Column(nullable = false, length = 64)
    String app;
    @Column(nullable = false, length = 128)
    String uri;
    @Column(nullable = false, length = 64)
    String ip;
    @Column(name = "created", nullable = false)
    LocalDateTime timestamp;
}