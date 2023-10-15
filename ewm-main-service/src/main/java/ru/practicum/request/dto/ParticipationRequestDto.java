package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.request.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMAT;

@Builder
@Getter
@Setter
public class ParticipationRequestDto {

    private long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime created;

    private long event;
    private long requester;
    private RequestStatus status;
}
