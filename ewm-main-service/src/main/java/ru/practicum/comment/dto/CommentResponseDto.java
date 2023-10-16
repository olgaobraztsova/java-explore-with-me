package ru.practicum.comment.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMAT;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;
    private EventShortDto event;
    private UserDto user;
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime posted;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime edited;
}
