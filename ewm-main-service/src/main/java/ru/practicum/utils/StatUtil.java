package ru.practicum.utils;

import ru.practicum.HitOutputDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatUtil {

    public static List<String> makeUrisWithEventIds(List<Long> eventIds) {
        return eventIds.stream()
                .map(eventId -> "/events/" + eventId)
                .collect(Collectors.toList());
    }

    public static Map<Long, Long> mapHitsToViewCountByEventId(List<HitOutputDto> hits) {
        return hits.stream()
                .collect(Collectors.toMap(
                        hit -> Long.parseLong(hit.getUri().split("/")[2]),
                        HitOutputDto::getHits
                ));
    }
}