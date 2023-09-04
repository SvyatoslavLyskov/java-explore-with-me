package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.dto.CompilationInputDto;
import ru.practicum.compilation.dto.CompilationOutputDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.event.model.State.PENDING;

@UtilityClass
public class ObjectMapper {
    public User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getEmail(),
                userDto.getName()
        );
    }

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public UserShortDto toUserShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }

    public List<UserDto> toUserDtoList(Iterable<User> users) {
        List<UserDto> result = new ArrayList<>();
        for (User user : users) {
            result.add(toUserDto(user));
        }
        return result;
    }

    public Request toRequest(RequestDto dto, User requester, Event event) {
        return new Request(
                dto.getCreated(),
                event,
                dto.getId(),
                requester,
                dto.getStatus()
        );
    }

    public RequestDto toRequestDto(Request request) {
        return new RequestDto(
                request.getCreated(),
                request.getEvent().getId(),
                request.getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }

    public List<RequestDto> toRequestDtoList(Iterable<Request> requests) {
        List<RequestDto> result = new ArrayList<>();

        for (Request request : requests) {
            result.add(toRequestDto(request));
        }
        return result;
    }

    public static Event toEvent(NewEventDto dto, Category category, Location location, User user) {
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
                .confirmedRequests(0L)
                .build();
    }

    public static EventFullDto toEventFullDto(Event event, Long views) {
        return new EventFullDto(
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }

    public static EventShortDto toEventShortDto(Event event, Long views) {
        User user = event.getInitiator();
        return new EventShortDto(
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                event.getId(),
                new UserShortDto(user.getId(), user.getName()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public static List<EventShortDto> toEventShortDtoList(Iterable<Event> events) {
        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventShortDto(event, 0L));
        }
        return result;
    }

    public static List<EventFullDto> toEventFullDtoList(Iterable<Event> events) {
        List<EventFullDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventFullDto(event, 0L));
        }
        return result;
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getLat(),
                location.getLon()
        );
    }

    public static Location toLocation(LocationDto locationDto) {
        return new Location(
                locationDto.getId(),
                locationDto.getLat(),
                locationDto.getLon()
        );
    }

    public Compilation toCompilation(CompilationInputDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

    public CompilationOutputDto toCompilationOutputDto(Compilation compilation) {
        List<EventShortDto> events = new ArrayList<>();
        if (compilation.getEvents() != null) {
            events = toEventShortDtoList(compilation.getEvents());
        }
        return new CompilationOutputDto(
                compilation.getId(),
                compilation.getTitle(),
                events,
                compilation.getPinned()
        );
    }

    public List<CompilationOutputDto> toCompilationDtoList(Iterable<Compilation> compilations) {
        List<CompilationOutputDto> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            result.add(toCompilationOutputDto(compilation));
        }
        return result;
    }

    public Category toCategory(CategoryDto dto) {
        return new Category(
                dto.getId(),
                dto.getName()
        );
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public List<CategoryDto> toCategoryDtoList(Iterable<Category> categories) {
        List<CategoryDto> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(toCategoryDto(category));
        }
        return result;
    }
}
