package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findEventByIdWithLock(@Param("eventId") Long eventId);

    List<Event> findFirstByCategoryId(Long categoryId);

    Set<Event> findByIdIn(Set<Long> ids);

    List<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    @Query("SELECT e FROM Event e " +
            "WHERE (COALESCE(:users, NULL) IS NULL OR e.initiator.id IN :users) " +
            "AND (COALESCE(:states, NULL) IS NULL OR e.state IN :states) " +
            "AND (COALESCE(:categories, NULL) IS NULL OR e.category.id IN :categories) " +
            "AND (COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd) ")
    List<Event> findAllEventsByAdmin(@Param("users") List<Long> users,
                                     @Param("states") List<State> states,
                                     @Param("categories") List<Long> categories,
                                     @Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd,
                                     PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (e.state = 'PUBLISHED') " +
            "AND (COALESCE(:text, NULL) IS NULL OR (lower(e.annotation) LIKE lower(concat('%', :text, '%')) " +
            "OR lower(e.description) LIKE lower(concat('%', :text, '%')))) " +
            "AND (COALESCE(:categories, NULL) IS NULL OR e.category.id IN :categories) " +
            "AND (COALESCE(:paid, NULL) IS NULL OR e.paid = :paid) " +
            "AND (COALESCE(:rangeStart, NULL) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (COALESCE(:rangeEnd, NULL) IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (e.confirmedRequests < e.participantLimit OR :onlyAvailable = FALSE)" +
            "GROUP BY e.id " +
            "ORDER BY LOWER(:sort) ASC")
    List<Event> findEventsByPublic(@Param("text") String text,
                                   @Param("categories") List<Long> categories,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStart") LocalDateTime startTime,
                                   @Param("rangeEnd") LocalDateTime endTime,
                                   @Param("onlyAvailable") Boolean onlyAvailable,
                                   @Param("sort") String sort,
                                   PageRequest pageRequest);
}