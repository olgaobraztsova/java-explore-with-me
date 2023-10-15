package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentResponseDto;

import java.util.List;

public interface CommentPublicService {

    List<CommentResponseDto> getCommentsByEvent(Long eventId);

    CommentResponseDto getCommentById(Long id);
}
