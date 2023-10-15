package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentPublicServiceImpl implements CommentPublicService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentResponseDto> getCommentsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));

        List<Comment> comments = commentRepository.findAllByEvent(event);

        return CommentMapper.entityListToDto(comments);
    }

    @Override
    public CommentResponseDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment with id = " + commentId + " doesn't exist", "object not found"));

        return CommentMapper.entityToDto(comment);
    }
}
