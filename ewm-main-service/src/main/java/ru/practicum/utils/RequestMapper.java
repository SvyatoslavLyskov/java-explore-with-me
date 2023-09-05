package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RequestMapper {
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
}
