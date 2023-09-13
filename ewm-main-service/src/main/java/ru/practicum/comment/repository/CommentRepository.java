package ru.practicum.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventId(Long eventId, PageRequest request);

    Optional<Comment> findByIdAndAuthorIdAndEventId(Long commentId, Long authorId, Long eventId);

    List<Comment> findAllByAuthorIdAndEventId(Long authorId, Long eventId);

    Page<Comment> findAllByAuthorIdAndEventIdAndTextContainingIgnoreCase(Long authorId, Long eventId,
                                                                         String text, Pageable pageable);

    List<Comment> findByCreatedBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest request);
}