package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.model.Status;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.RequestMapper;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.user.service.UserService.checkUserAvailability;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        Event event = eventRepository.findEventByIdWithLock(eventId).orElseThrow(() ->
                new NotFoundException("Событие", eventId));
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException(String.format(
                    "Пользователь %d является инициатором события %d.", userId, eventId));
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException(String.format("Превышен лимит запросов к событию %d.", eventId));
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
            return RequestMapper.toRequestDto(request);
        }

        request = requestRepository.save(request);
        log.info("Пользователь {} отправил запрос на участие в мероприятии {}.", user.getName(), eventId);
        return RequestMapper.toRequestDto(request);
    }

    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        checkUserAvailability(userRepository, userId);
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Request", requestId));
        request.setStatus(Status.CANCELED);
        Request savedRequest = requestRepository.save(request);
        log.info("Пользователь {} отменил заявку {}.", userId, requestId);
        return RequestMapper.toRequestDto(savedRequest);
    }

    public List<RequestDto> findRequestsByUserId(Long userId) {
        checkUserAvailability(userRepository, userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        log.info("Найдены события пользователя {}.", userId);
        return RequestMapper.toRequestDtoList(requests);
    }
}
