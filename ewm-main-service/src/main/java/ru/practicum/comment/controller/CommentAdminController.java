package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/admin/comments")
public class CommentAdminController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCommentByAdmin(@PathVariable Long commentId) {
        commentService.removeCommentByAdmin(commentId);
    }

    @GetMapping
    public List<CommentDto> findAllByAdmin(
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @Past @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
            @RequestParam(required = false) @PastOrPresent @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd) {
        return commentService.findAllByAdmin(rangeStart, rangeEnd, from, size);
    }
}