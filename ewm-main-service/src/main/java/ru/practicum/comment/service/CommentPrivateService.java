package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentPrivateService {

    CommentResponseDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId);

    CommentResponseDto editComment(NewCommentDto commentDto, Long commentId, Long userId);

    void deleteComment(Long commentId, Long userId);

    List<CommentResponseDto> getAllCommentsByUserForEvent(Long userId, Long eventId);
}
