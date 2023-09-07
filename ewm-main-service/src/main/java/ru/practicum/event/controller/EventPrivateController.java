package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @Validated(Create.class) @RequestBody CreateEventDto dto) {
        return eventService.createEvent(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> getAllInitiatorEvents(@PathVariable Long userId,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getAllInitiatorEvents(userId, from, size);
    }

    @GetMapping(path = "/{eventId}")
    public EventFullDto getInitiatorEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getInitiatorEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateInitiatorEvent(@RequestBody @Validated(Update.class) UpdateEventUserDto eventUpdateDto,
                                             @PathVariable Long userId, @PathVariable Long eventId) {

        return eventService.updateInitiatorEvent(eventUpdateDto, userId, eventId);
    }

    @GetMapping(path = "/{eventId}/requests")
    public List<RequestDto> findEventRequestsByInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.findEventRequestsByInitiator(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public RequestStatusUpdateResultDto updateStatusRequestsByUserId(@RequestBody RequestStatusUpdateRequestDto request,
                                                                     @PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.updateStatusRequestsByUserId(request, userId, eventId);
    }
}
