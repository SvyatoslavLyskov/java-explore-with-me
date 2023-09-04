package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findEventById(@PathVariable Long id, HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        return eventService.findEventById(id, uri, ip);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findAllEvents(@RequestParam(required = false) String text,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) Boolean paid,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd,
                                             @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                             @RequestParam(required = false) String sort,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                             @Positive @RequestParam(defaultValue = "10") Integer size,
                                             HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        return eventService.findAllEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                uri, ip);
    }
}
