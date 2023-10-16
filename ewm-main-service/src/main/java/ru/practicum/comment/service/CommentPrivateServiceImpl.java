package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentResponseDto createComment(NewCommentDto newCommentDto, Long eventId, Long userId) {
        if (eventId == null) {
            throw new BadParameterException(
                    "Указание события обязательно при добавлении комментария", "Bad Parameter");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        // нельзя комментировать неопубликованное событие (Ожидается код ошибки 409)
        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Commenting is possible for PUBLISHED events",
                    "For the requested operation the conditions are not met.");
        }

        Comment comment = Comment.builder()
                .event(event)
                .user(user)
                .text(newCommentDto.getText())
                .posted(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.entityToDto(savedComment);
    }

    @Override
    public CommentResponseDto editComment(NewCommentDto commentDto, Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment with id = " + commentId + " doesn't exist", "object not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!comment.getUser().equals(user)) {
            throw new ConflictException("Пользователь не имеет прав редактировать данный комментарий", "Access Denied");
        }

        if (commentDto != null && !commentDto.getText().isEmpty()) {
            comment.setText(commentDto.getText());
            comment.setEdited(LocalDateTime.now());
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.entityToDto(savedComment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment with id = " + commentId + " doesn't exist", "object not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!comment.getUser().equals(user)) {
            throw new ConflictException("Пользователь не имеет прав удалять данный комментарий", "Access Denied");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentResponseDto> getAllCommentsByUserForEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        List<Comment> comments = commentRepository.findAllByEventAndUserOrderByIdAsc(event, user);

        return CommentMapper.entityListToDto(comments);
    }
}
