package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentPrivateService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}")
@Validated
public class CommentPrivateController {

    private final CommentPrivateService commentPrivateService;

    @PostMapping(path = "/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                            @PathVariable long eventId,
                                            @PathVariable long userId) {
        return commentPrivateService.createComment(newCommentDto, eventId, userId);
    }

    @PatchMapping(path = "/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto editComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                          @PathVariable long commentId,
                                          @PathVariable long userId) {
        return commentPrivateService.editComment(newCommentDto, commentId, userId);
    }

    @DeleteMapping("/events/{eventId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long commentId, @PathVariable long userId) {
        commentPrivateService.deleteComment(commentId, userId);
    }

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getAllCommentsByUserForEvent(
            @PathVariable long userId, @PathVariable long eventId) {
        return commentPrivateService.getAllCommentsByUserForEvent(userId, eventId);
    }
}
