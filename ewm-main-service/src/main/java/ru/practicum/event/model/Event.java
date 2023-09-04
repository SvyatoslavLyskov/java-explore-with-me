package ru.practicum.event.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    Long id;
    @Column(length = 2000)
    String annotation;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;
    @Column(length = 120)
    String title;
    @Column(length = 7000)
    String description;
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", nullable = false)
    Location location;
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    Boolean paid;
    @Column(name = "confirmed_requests")
    Long confirmedRequests;
    @Column(name = "participant_limit")
    Long participantLimit;
    @Column(name = "request_moderation", columnDefinition = "BOOLEAN DEFAULT true")
    Boolean requestModeration;
    @Column(name = "published_on")
    LocalDateTime publishedOn;
    @Column(name = "created_on")
    LocalDateTime createdOn;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 9)
    State state;
}
