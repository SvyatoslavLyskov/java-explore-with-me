package ru.practicum.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitClient;
import ru.practicum.HitOutputDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UnionService {
    private final HitClient hitClient;

    public List<HitOutputDto> getViews(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime startDate = getEarliestPublicationDate(events);
        if (startDate == null) {
            return Collections.emptyList();
        }
        List<String> uris = StatUtil.makeUrisWithEventIds(eventIds);
        List<HitOutputDto> response = hitClient.getHitStats(startDate, LocalDateTime.now(), uris, true);
        log.info("Отправлен запрос на получение статистики {}.", uris);
        return response;
    }

    private LocalDateTime getEarliestPublicationDate(List<Event> events) {
        LocalDateTime earliestDate = null;
        for (Event event : events) {
            LocalDateTime publicationDate = event.getPublishedOn();
            if (publicationDate != null && (earliestDate == null || publicationDate.isBefore(earliestDate))) {
                earliestDate = publicationDate;
            }
        }
        return earliestDate;
    }
}