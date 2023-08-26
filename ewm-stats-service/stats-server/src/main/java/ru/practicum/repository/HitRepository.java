package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Hit;
import ru.practicum.HitOutputDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query("SELECT new ru.practicum.HitOutputDto(h.app, h.uri, count(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN :uris " +
            "GROUP BY h.uri, h.app  " +
            "ORDER BY count(h.ip) DESC")
    List<HitOutputDto> findAllByTimestampAndUrisAndUniqueIp(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);


    @Query("SELECT new ru.practicum.HitOutputDto(h.app, h.uri, count(h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN :uris " +
            "GROUP BY h.uri, h.app  " +
            "ORDER BY count(h.ip) DESC")
    List<HitOutputDto> findAllByTimestampAndUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.HitOutputDto(h.app, h.uri, count(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.uri, h.app  " +
            "ORDER BY count(h.ip) DESC")
    List<HitOutputDto> findAllByTimestampAndUniqueIp(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    @Query("SELECT new ru.practicum.HitOutputDto(h.app, h.uri, count(h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.uri, h.app " +
            "ORDER BY count(h.ip) DESC")
    List<HitOutputDto> findAllByTimestamp(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}