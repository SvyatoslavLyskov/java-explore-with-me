package ru.practicum.mapper;

import ru.practicum.HitInputDto;
import ru.practicum.model.Hit;

public class HitMapper {
    public static Hit toHit(HitInputDto hitInputDto) {
        return new Hit(
                hitInputDto.getId(),
                hitInputDto.getApp(),
                hitInputDto.getUri(),
                hitInputDto.getIp(),
                hitInputDto.getTimestamp()
        );
    }

    public HitInputDto toHitDto(Hit hit) {
        return new HitInputDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }
}