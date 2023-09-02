package ru.practicum.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.Status;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.ObjectMapper;
import ru.practicum.request.model.Request;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Transactional
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        Event event = eventRepository.findEventByIdWithLock(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException(String.format(
                    "Пользователь %d является инициатором события %d.", userId, eventId));
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException(String.format("Количество запросов к событию %d превышает лимит.", eventId));
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(String.format("Событие %d ещё не опубликовано.", eventId));
        }
        Request request = Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(Status.PENDING)
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
            request = requestRepository.save(request);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1L);
            eventRepository.save(event);
            return ObjectMapper.toRequestDto(request);
        }
        request = requestRepository.save(request);
        log.info("Пользователь {} отправил запрос на участие в мероприятии {}.", user.getName(), eventId);
        return ObjectMapper.toRequestDto(request);
    }

    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        userService.checkUserAvailability(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request", requestId));
        request.setStatus(Status.CANCELED);
        Request savedRequest = requestRepository.save(request);
        log.info("Пользователь {} отменил заявку {}.", userId, requestId);
        return ObjectMapper.toRequestDto(savedRequest);
    }

    public List<RequestDto> findRequestsByUserId(Long userId) {
        userService.checkUserAvailability(userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        log.info("Найдены заявки для пользователя {}.", userId);
        return ObjectMapper.toRequestDtoList(requests);
    }
}
