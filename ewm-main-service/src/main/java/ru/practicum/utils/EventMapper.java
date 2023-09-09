package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.BaseEventDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.event.model.State.PENDING;

@UtilityClass
public class EventMapper {
    public Event toEvent(BaseEventDto dto, Category category, Location location, User user) {
        return Event.builder()
                .category(category)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(user)
                .paid(dto.getPaid())
                .location(location)
                .annotation(dto.getAnnotation())
                .state(PENDING)
                .participantLimit(dto.getParticipantLimit())
                .createdOn(LocalDateTime.now())
                .requestModeration(dto.getRequestModeration())
                .build();
    }

    public EventFullDto toEventFullDto(Event event, Long views) {
        return new EventFullDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                LocationMapper.toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }

    public EventShortDto toEventShortDto(Event event, Long views) {
        User user = event.getInitiator();
        return new EventShortDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                event.getId(),
                new UserShortDto(user.getId(), user.getName()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public List<EventShortDto> toEventShortDtoList(Iterable<Event> events) {
        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventShortDto(event, 0L));
        }
        return result;
    }

    public List<EventFullDto> toEventFullDtoList(Iterable<Event> events) {
        List<EventFullDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventFullDto(event, 0L));
        }
        return result;
    }
}
