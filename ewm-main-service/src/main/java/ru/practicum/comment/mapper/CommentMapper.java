package ru.practicum.comment.mapper;

import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class CommentMapper {

    public static CommentResponseDto entityToDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .event(EventMapper.toEventDtoShort(comment.getEvent()))
                .user(UserMapper.entityToDto(comment.getUser()))
                .text(comment.getText())
                .posted(comment.getPosted())
                .edited(comment.getEdited())
                .build();
    }

    public static List<CommentResponseDto> entityListToDto(List<Comment> comments) {
        if (comments.isEmpty()) {
            return emptyList();
        }
        return comments.stream().map(CommentMapper::entityToDto).collect(toList());
    }
}
