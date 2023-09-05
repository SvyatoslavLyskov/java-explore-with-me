package ru.practicum.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitClient;
import ru.practicum.HitOutputDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ru.practicum.utils.DateTimeFormat.START;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UnionService {
    private final HitClient hitClient;
    private final EventRepository eventRepository;

    public List<HitOutputDto> getViews(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime startDate = getEarliestPublicationDate(eventIds);
        if (startDate == null) {
            return Collections.emptyList();
        }
        List<String> uris = StatUtil.makeUrisWithEventIds(eventIds);
        List<HitOutputDto> response = hitClient.getHitStats(startDate, LocalDateTime.now(), uris, true);
        log.info("Отправлен запрос на получение статистики {}.", uris);
        return response;
    }

    private LocalDateTime getEarliestPublicationDate(List<Long> eventIds) {
        LocalDateTime earliestDate = null;
        for (Long eventId : eventIds) {
            LocalDateTime publicationDate = getPublicationDate(eventId);
            if (publicationDate != null && (earliestDate == null || publicationDate.isBefore(earliestDate))) {
                    earliestDate = publicationDate;

            }
        }
        return earliestDate;
    }

    private LocalDateTime getPublicationDate(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event != null) {
            return event.getPublishedOn();
        }
        return null;
    }
}