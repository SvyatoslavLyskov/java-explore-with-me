package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RequestDto> addRequest(@PathVariable Long userId,
                                                 @Positive @RequestParam Long eventId) {
        RequestDto requestDto = requestService.addRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestDto);
    }

    @GetMapping
    public ResponseEntity<List<RequestDto>> findRequestsByUserId(@PathVariable Long userId) {
        List<RequestDto> requestDtos = requestService.findRequestsByUserId(userId);
        return ResponseEntity.ok(requestDtos);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable Long userId,
                                                    @PathVariable Long requestId) {
        RequestDto requestDto = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(requestDto);
    }
}
