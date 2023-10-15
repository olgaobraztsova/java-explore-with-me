package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentPublicService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class CommentPublicController {

    private final CommentPublicService commentPublicService;

    @GetMapping(path = "/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentResponseDto> getCommentsByEvent(@PathVariable long eventId) {
        return commentPublicService.getCommentsByEvent(eventId);
    }

    @GetMapping(path = "/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getCommentById(@PathVariable long commentId) {
        return commentPublicService.getCommentById(commentId);
    }
}
