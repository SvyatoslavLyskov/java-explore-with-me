package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.HitOutputDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class StatUtil {

    public List<String> makeUrisWithEventIds(List<Long> eventIds) {
        List<String> uris = new ArrayList<>();
        for (Long eventId : eventIds) {
            String uri = "/events/" + eventId;
            uris.add(uri);
        }
        return uris;
    }

    public Map<Long, Long> mapHitsToViewCountByEventId(List<HitOutputDto> hits) {
        Map<Long, Long> views = new HashMap<>();
        for (HitOutputDto hit : hits) {
            String[] split = hit.getUri().split("/");
            String id = split[2];
            views.put(Long.parseLong(id), hit.getHits());
        }
        return views;
    }
}
