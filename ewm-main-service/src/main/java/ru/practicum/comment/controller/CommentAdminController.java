package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentAdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events/comments")
@Validated
public class CommentAdminController {

    private final CommentAdminService commentAdminService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long commentId) {
        commentAdminService.deleteComment(commentId);
    }

}
