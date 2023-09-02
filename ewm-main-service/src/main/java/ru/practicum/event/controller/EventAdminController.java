package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventService;
import ru.practicum.validation.Update;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findAllEventsByAdmin(@RequestParam(required = false) List<Long> users,
                @RequestParam(required = false) List<State> states,
                @RequestParam(required = false) List<Long> categories,
                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd,
                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                @RequestParam(defaultValue = "10") @Positive Integer size) {
        return eventService.findAllEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Validated(Update.class) @RequestBody NewEventDto eventDto) {
        return eventService.updateEventByAdmin(eventId, eventDto);
    }
}
