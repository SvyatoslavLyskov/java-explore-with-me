package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitClient;
import ru.practicum.HitInputDto;
import ru.practicum.HitOutputDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.model.Status;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UnsupportedException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.dto.UpdateEventAdminDto.StateAction.PUBLISH_EVENT;
import static ru.practicum.event.dto.UpdateEventAdminDto.StateAction.REJECT_EVENT;
import static ru.practicum.event.model.State.PUBLISHED;
import static ru.practicum.event.model.Status.CONFIRMED;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;
    private final UnionService unionService;
    @Value("${app.name}")
    private String appName;

    @Transactional
    public EventFullDto createEvent(Long userId, CreateEventDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь", userId));
        Long categoryId = dto.getCategory();
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Категория", categoryId));
        Location location = LocationMapper.toLocation(dto.getLocation());
        locationRepository.save(location);
        if (dto.getPaid() == null) {
            dto.setPaid(false);
        }
        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(true);
        }
        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(0L);
        }
        Event savedEvent = eventRepository.save(EventMapper.toEvent(dto, category, location, user));
        log.info("Событие {} добавлено.", savedEvent.getId());
        return EventMapper.toEventFullDto(savedEvent, 0L);
    }

    public List<EventShortDto> getAllInitiatorEvents(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);
        log.info("Найден список из {} событий пользователя {}.", events.size(), userId);
        return EventMapper.toEventShortDtoList(events);
    }

    public EventFullDto getInitiatorEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие", eventId));
        checkInitiator(userId, eventId);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        log.info("Найдено событие {} пользователя {}.", eventId, userId);
        return EventMapper.toEventFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    public List<RequestDto> findEventRequestsByInitiator(Long userId, Long eventId) {
        checkInitiator(userId, eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        log.info("Найдены запросы на участие в событии {}, опубликованном пользователем {}.", eventId, userId);
        return RequestMapper.toRequestDtoList(requests);
    }

    @Transactional
    public EventFullDto updateInitiatorEvent(UpdateEventDto eventUpdateDto, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие", eventId));
        checkInitiator(userId, eventId);
        if (event.getState().equals(PUBLISHED)) {
            throw new ConflictException(String.format(
                    "Пользователь %s не может изменить событие %s", userId, eventId));
        }
        Event updatedEvent = updateEvent(event, eventUpdateDto);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return EventMapper.toEventFullDto(updatedEvent, views.getOrDefault(event.getId(), 0L));
    }

    @Transactional
    public RequestStatusUpdateResultDto updateStatusRequestsByUserId(RequestStatusUpdateRequestDto request,
                                                                     Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие", eventId));
        RequestStatusUpdateResultDto result = RequestStatusUpdateResultDto.builder()
                .confirmedRequests(Collections.emptyList())
                .rejectedRequests(Collections.emptyList())
                .build();
        checkInitiator(userId, eventId);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Превышен лимит участников");
        }
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        long vacantPlace = event.getParticipantLimit() - event.getConfirmedRequests();
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        for (Request nextRequest : requests) {
            if (!nextRequest.getStatus().equals(Status.PENDING)) {
                throw new ConflictException("Запрос должен иметь статус PENDING");
            }
            if (request.getStatus() == (RequestStatusUpdateRequestDto.Status.CONFIRMED) && vacantPlace > 0) {
                nextRequest.setStatus(CONFIRMED);
                confirmedRequests.add(nextRequest);
                vacantPlace--;
            } else {
                nextRequest.setStatus(Status.REJECTED);
                rejectedRequests.add(nextRequest);
            }
        }
        result.setConfirmedRequests(RequestMapper.toRequestDtoList(confirmedRequests));
        result.setRejectedRequests(RequestMapper.toRequestDtoList(rejectedRequests));
        return result;
    }

    public EventFullDto findEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Событие", id));
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException(String.format("Событие %s не опубликовано", id));
        }
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        sendStats(uri, ip);
        List<HitOutputDto> hits = unionService.getViews(List.of(id));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return EventMapper.toEventFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    public List<EventShortDto> findAllEvents(String text, List<Long> categories, Boolean paid,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Boolean onlyAvailable, String sort, Integer from,
                                             Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && (rangeStart.isAfter(rangeEnd))) {
            throw new UnsupportedException("Дата окончания раньше даты начала.");
        }
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findEventsByPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageRequest);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        sendStats(uri, ip);
        List<HitOutputDto> hits = unionService.getViews(eventIds);
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        List<EventShortDto> result = EventMapper.toEventShortDtoList(events);
        for (EventShortDto event : result) {
            event.setViews(views.get(event.getId()));
        }
        return result;
    }

    public List<EventFullDto> findAllEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllEventsByAdmin(
                users, states, categories, rangeStart, rangeEnd, pageRequest);
        List<Long> eventIds = new ArrayList<>();
        for (Event event : events) {
            eventIds.add(event.getId());
        }
        List<HitOutputDto> hits = unionService.getViews(eventIds);
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        List<EventFullDto> result = EventMapper.toEventFullDtoList(events);
        for (EventFullDto event : result) {
            event.setViews(views.getOrDefault(event.getId(), 0L));
        }
        return result;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminDto eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие", eventId));
        if (eventDto.getStateAction() != null) {
            UpdateEventDto.StateAction updateEventStateAction = eventDto.getStateAction();
            UpdateEventAdminDto.StateAction adminEventStateAction;
            switch (updateEventStateAction) {
                case PUBLISH_EVENT:
                    adminEventStateAction = PUBLISH_EVENT;
                    break;
                case REJECT_EVENT:
                    adminEventStateAction = REJECT_EVENT;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid state action: " + updateEventStateAction);
            }
            if (adminEventStateAction == PUBLISH_EVENT) {
                if (!event.getState().equals(State.PENDING)) {
                    throw new ConflictException(String.format(
                            "Событие - %s, не может быть опубликовано повторно.", event.getTitle()));
                }
                event.setPublishedOn(LocalDateTime.now());
                event.setState(State.PUBLISHED);
            } else {
                if (!event.getState().equals(State.PENDING)) {
                    throw new ConflictException(String.format(
                            "Событие - %s, не может быть отменено без статуса \"PENDING\"", event.getTitle()));
                }
                event.setState(State.CANCELED);
            }
        }
        Event updatedEvent = updateEvent(event, eventDto);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return EventMapper.toEventFullDto(updatedEvent, views.getOrDefault(event.getId(), 0L));
    }

    private Event updateEvent(Event event, UpdateEventDto eventUpdateDto) {
        updateAnnotation(event, eventUpdateDto);
        updateCategory(event, eventUpdateDto);
        updateDescription(event, eventUpdateDto);
        updateEventDate(event, eventUpdateDto);
        updateLocation(event, eventUpdateDto);
        updatePaid(event, eventUpdateDto);
        updateParticipantLimit(event, eventUpdateDto);
        updateRequestModeration(event, eventUpdateDto);
        updateState(event, eventUpdateDto);
        updateTitle(event, eventUpdateDto);

        locationRepository.save(event.getLocation());
        return eventRepository.save(event);
    }

    private void updateAnnotation(Event event, UpdateEventDto eventUpdateDto) {
        String annotation = eventUpdateDto.getAnnotation();
        if (annotation != null && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private void updateCategory(Event event, UpdateEventDto eventUpdateDto) {
        Long categoryId = eventUpdateDto.getCategory();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория", categoryId));
            event.setCategory(category);
        }
    }

    private void updateDescription(Event event, UpdateEventDto eventUpdateDto) {
        String description = eventUpdateDto.getDescription();
        if (description != null && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventDate(Event event, UpdateEventDto eventUpdateDto) {
        LocalDateTime eventDate = eventUpdateDto.getEventDate();
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
    }

    private void updateLocation(Event event, UpdateEventDto eventUpdateDto) {
        LocationDto locationDto = eventUpdateDto.getLocation();
        if (locationDto != null) {
            event.setLocation(LocationMapper.toLocation(locationDto));
        }
    }

    private void updatePaid(Event event, UpdateEventDto eventUpdateDto) {
        Boolean paid = eventUpdateDto.getPaid();
        if (paid != null) {
            event.setPaid(paid);
        }
    }

    private void updateParticipantLimit(Event event, UpdateEventDto eventUpdateDto) {
        Long participantLimit = eventUpdateDto.getParticipantLimit();
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
    }

    private void updateRequestModeration(Event event, UpdateEventDto eventUpdateDto) {
        Boolean requestModeration = eventUpdateDto.getRequestModeration();
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateState(Event event, UpdateEventDto eventUpdateDto) {
        UpdateEventDto.StateAction stateAction = eventUpdateDto.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case PUBLISH_EVENT:
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
            }
        }
    }

    private void updateTitle(Event event, UpdateEventDto eventUpdateDto) {
        String title = eventUpdateDto.getTitle();
        if (title != null && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void checkInitiator(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие", eventId));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException(String.format(
                    "Пользователь %s не является инициатором события %s.", userId, eventId));
        }
    }

    private void sendStats(String uri, String ip) {
        HitInputDto hitDto = HitInputDto.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        hitClient.addHit(hitDto);
    }
}
