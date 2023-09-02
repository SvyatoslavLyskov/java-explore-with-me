package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitClient;
import ru.practicum.HitInputDto;
import ru.practicum.HitOutputDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.dto.*;
import ru.practicum.utils.ObjectMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.State;
import ru.practicum.event.model.Status;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UnsupportedException;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;
import ru.practicum.utils.StatUtil;
import ru.practicum.utils.UnionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        Long categoryId = dto.getCategory();
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Категория", categoryId));
        Location location = ObjectMapper.toLocation(dto.getLocation());
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
        Event savedEvent = eventRepository.save(ObjectMapper.toEvent(dto, category, location, user));
        log.info("Событие {} успешно добавлено.", savedEvent.getId());
        return ObjectMapper.toEventFullDto(savedEvent, 0L);
    }

    public List<EventShortDto> getAllInitiatorEvents(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);
        log.info("Найден список из {} событий, опубликованных пользователем {}.", events.size(), userId);
        return ObjectMapper.toEventShortDtoList(events);
    }

    public EventFullDto getInitiatorEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        checkInitiator(userId,eventId);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        log.info("Найдено событие {}, опубликованное пользователем {}.",eventId, userId);
        return ObjectMapper.toEventFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    public List<RequestDto> findEventRequestsByInitiator(Long userId, Long eventId) {
        checkInitiator(userId,eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        log.info("Найдены запросы на участие в событии {}, опубликованноe пользователем {}.",eventId, userId);
        return ObjectMapper.toRequestDtoList(requests);
    }

    @Transactional
    public EventFullDto updateInitiatorEvent(NewEventDto eventUpdateDto, Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
      checkInitiator(userId,eventId);
        if (event.getState().equals(PUBLISHED)) {
            throw new ConflictException(String.format(
                    "Пользователь %s не может изменить опубликованное событие %s",userId, eventId));
        }
        Event updatedEvent = updateEvent(event, eventUpdateDto);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return ObjectMapper.toEventFullDto(updatedEvent, views.getOrDefault(event.getId(), 0L));
    }

    @Transactional
    public RequestStatusUpdateResultDto updateStatusRequestsByUserId(RequestStatusUpdateRequestDto request,
                                                                     Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        RequestStatusUpdateResultDto result = RequestStatusUpdateResultDto.builder()
                .confirmedRequests(Collections.emptyList())
                .rejectedRequests(Collections.emptyList())
                .build();
        checkInitiator(userId,eventId);
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
            if (request.getStatus().equals(CONFIRMED) && vacantPlace > 0) {
                nextRequest.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1L);
                confirmedRequests.add(nextRequest);
                vacantPlace--;
            } else {
                nextRequest.setStatus(Status.REJECTED);
                rejectedRequests.add(nextRequest);
            }
        }
        result.setConfirmedRequests(ObjectMapper.toRequestDtoList(confirmedRequests));
        result.setRejectedRequests(ObjectMapper.toRequestDtoList(rejectedRequests));
        return result;
    }

    public EventFullDto findEventById(Long id, String uri, String ip) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Событие", id));
        if (!event.getState().equals(PUBLISHED)) {
            throw new NotFoundException(String.format("Событие %s не опубликовано", id));
        }
        sendStats(uri, ip);
        List<HitOutputDto> hits = unionService.getViews(List.of(id));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return ObjectMapper.toEventFullDto(event, views.getOrDefault(event.getId(), 0L));
    }

    public List<EventShortDto> findAllEvents(String text, List<Long> categories, Boolean paid,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Boolean onlyAvailable, String sort, Integer from,
                                             Integer size, String uri, String ip) {
        validateDateRange(rangeStart, rangeEnd);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findEventsByPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageRequest);
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        sendStats(uri, ip);
        List<HitOutputDto> hits = unionService.getViews(eventIds);
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        List<EventShortDto> result = ObjectMapper.toEventShortDtoList(events);
        result.forEach(event -> event.setViews(views.get(event.getId())));
        return result;
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new UnsupportedException("Дата окончания раньше даты начала.");
        }
    }

    public List<EventFullDto> findAllEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                   Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllEventsByAdmin(
                users, states, categories, rangeStart, rangeEnd, pageRequest);
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        List<HitOutputDto> hits = unionService.getViews(eventIds);
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        List<EventFullDto> result = ObjectMapper.toEventFullDtoList(events);
        result.forEach(event -> event.setViews(views.getOrDefault(event.getId(), 0L)));
        return result;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, NewEventDto eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        if (eventDto.getStateAction() != null) {
            validateAndUpdateEventState(event, eventDto.getStateAction());
        }
        Event updateEvent = updateEvent(event, eventDto);
        List<HitOutputDto> hits = unionService.getViews(List.of(eventId));
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        return ObjectMapper.toEventFullDto(updateEvent, views.getOrDefault(event.getId(), 0L));
    }

    private void validateAndUpdateEventState(Event event, StateAction stateAction) {
        if (stateAction.equals(StateAction.PUBLISH_EVENT)) {
            if (!event.getState().equals(State.PENDING)) {
                throw new ConflictException(String.format(
                        "Событие - %s, уже опубликовано.", event.getTitle()));
            }
            event.setPublishedOn(LocalDateTime.now());
            event.setState(State.PUBLISHED);
        } else {
            if (!event.getState().equals(State.PENDING)) {
                throw new ConflictException(String.format(
                        "Событие - %s, не может быть отменено в статусе \"PENDING\"", event.getTitle()));
            }
            event.setState(State.CANCELED);
        }
    }

    private Event updateEvent(Event event, NewEventDto eventUpdateDto) {
        if (eventUpdateDto.getAnnotation() != null && !eventUpdateDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventUpdateDto.getAnnotation());
        }
        Long categoryId = eventUpdateDto.getCategory();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория", categoryId));
            event.setCategory(category);
        }
        if (eventUpdateDto.getDescription() != null && !eventUpdateDto.getDescription().isBlank()) {
            event.setDescription(eventUpdateDto.getDescription());
        }
        if (eventUpdateDto.getEventDate() != null) {
            event.setEventDate(eventUpdateDto.getEventDate());
        }
        if (eventUpdateDto.getLocation() != null) {
            event.setLocation(ObjectMapper.toLocation(eventUpdateDto.getLocation()));
        }
        if (eventUpdateDto.getPaid() != null) {
            event.setPaid(eventUpdateDto.getPaid());
        }
        if (eventUpdateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventUpdateDto.getParticipantLimit());
        }
        if (eventUpdateDto.getRequestModeration() != null) {
            event.setRequestModeration(eventUpdateDto.getRequestModeration());
        }
        StateAction stateAction = eventUpdateDto.getStateAction();
        if (stateAction != null) {
            updateEventState(event, stateAction);
        }
        if (eventUpdateDto.getTitle() != null && !eventUpdateDto.getTitle().isBlank()) {
            event.setTitle(eventUpdateDto.getTitle());
        }
        locationRepository.save(event.getLocation());
        return eventRepository.save(event);
    }

    private void updateEventState(Event event, StateAction stateAction) {
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
            default:
                break;
        }
    }

    private void checkInitiator(Long userId, Long eventId){
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException(String.format(
                    "Пользователь %s не является инициатором события %s.",userId, eventId));
        }
    }

    private void sendStats(String uri, String ip) {
        HitInputDto hitDto = HitInputDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        hitClient.addHit(hitDto);
    }
}
