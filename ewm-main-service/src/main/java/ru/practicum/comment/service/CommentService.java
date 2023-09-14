package ru.practicum.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UnsupportedException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.CommentMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private static void checkAuthorComment(Long userId, Comment comment) {
        if (!Objects.equals(userId, comment.getAuthor().getId())) {
            throw new ConflictException("Изменение комментария доступно только автору.");
        }
    }

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        Comment savedComment = commentRepository.save(CommentMapper.toComment(commentDto, user, event));
        log.info("Комментарий {} добавлен.", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, Long eventId, CommentDto commentDto) {
        validateUserAndEventAvailability(userId, eventId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий", commentId));
        checkAuthorComment(userId, comment);
        comment.setText(commentDto.getText());
        comment.setEdited(true);
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий {} обновлен.", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment);
    }

    @Transactional
    public void removeCommentByUser(Long userId, Long eventId, Long commentId) {
        validateUserAndEventAvailability(userId, eventId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментарий", commentId));
        checkAuthorComment(userId, comment);
        commentRepository.deleteById(commentId);
        log.info("Комментарий {} успешно удален.", commentId);
    }

    @Transactional
    public void removeCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий", commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий {} удален администратором.", commentId);
    }

    public CommentDto findCommentById(Long userId, Long eventId, Long commentId) {
        validateUserAndEventAvailability(userId, eventId);
        Comment comment = commentRepository.findByIdAndAuthorIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new NotFoundException("Комментарий", commentId));
        log.info("Комментарий {} найден.", commentId);
        return CommentMapper.toCommentDto(comment);
    }

    public List<CommentDto> findAll(Long userId, Long eventId) {
        validateUserAndEventAvailability(userId, eventId);
        List<Comment> comments = commentRepository.findAllByAuthorIdAndEventId(userId, eventId);
        log.info("Получен список из {} комментариев.", comments.size());
        return CommentMapper.toCommentDtoList(comments);
    }

    public Page<CommentDto> findCommentByText(Long userId, Long eventId, String text, Integer from, Integer size) {
        validateUserAndEventAvailability(userId, eventId);
        if (text.isBlank()) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> commentPage = commentRepository.findAllByAuthorIdAndEventIdAndTextContainingIgnoreCase(
                userId, eventId, text, pageable);
        log.info("Получена страница комментариев, количество: {}", commentPage.getTotalElements());
        return commentPage.map(CommentMapper::toCommentDto);
    }

    public List<CommentDto> findAllByAdmin(
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new UnsupportedException("Дата окончания раньше даты начала.");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Comment> comments = (rangeStart != null && rangeEnd != null)
                ? commentRepository.findByCreatedBetween(rangeStart, rangeEnd, pageRequest)
                : commentRepository.findAll(pageRequest).getContent();
        log.info("Получен список из {} комментариев.", comments.size());
        return CommentMapper.toCommentDtoList(comments);
    }

    public List<CommentDto> findAllByEventId(Long eventId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventId(eventId, pageRequest);
        log.info("Получен список из {} комментариев.", comments.size());
        return CommentMapper.toCommentDtoList(comments);
    }

    private User validateAndRetrieveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь", userId));
    }

    private Event validateAndRetrieveEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие", eventId));
    }

    private void validateUserAndEventAvailability(Long userId, Long eventId) {
        validateAndRetrieveUser(userId);
        validateAndRetrieveEvent(eventId);
    }
}