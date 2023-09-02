package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.UnsupportedException;
import ru.practicum.mapper.HitMapper;
import ru.practicum.HitInputDto;
import ru.practicum.HitOutputDto;
import ru.practicum.repository.HitRepository;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HitService {
    private final HitRepository hitRepository;

    @Transactional
    public void addHit(HitInputDto hitInputDto) {
        Hit hit = HitMapper.toHit(hitInputDto);
        Hit savedHit = hitRepository.save(hit);
        log.info("Успешно добавлен {} с id {}.", savedHit.getApp(), savedHit.getId());
    }

    public List<HitOutputDto> getHitStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<HitOutputDto> hitStats;
        if (start.isAfter(end)) {
            throw new UnsupportedException("Дата начала раньше даты окончания.");
        }
        if (uris != null) {
            if (unique) {
                hitStats = hitRepository.findAllByTimestampAndUrisAndUniqueIp(start, end, uris);
                log.info("Получена статистика по uri и уникальному ip.");
            } else {
                hitStats = hitRepository.findAllByTimestampAndUris(start, end, uris);
                log.info("Получена статистика по uri.");
            }
        } else {
            if (unique) {
                hitStats = hitRepository.findAllByTimestampAndUniqueIp(start, end);
                log.info("Получена статистика по уникальному ip.");
            } else {
                hitStats = hitRepository.findAllByTimestamp(start, end);
                log.info("Получена статистика.");
            }
        }
        return hitStats;
    }
}