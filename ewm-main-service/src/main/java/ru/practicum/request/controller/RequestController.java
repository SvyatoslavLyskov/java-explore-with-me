package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto addRequest(@PathVariable Long userId, @Positive @RequestParam Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> findRequestsByUserId(@PathVariable Long userId) {
        return requestService.findRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
