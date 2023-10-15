package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class RequestMapper {
    public static ParticipationRequestDto entityToDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> entityListToDto(List<Request> requests) {
        if (requests.isEmpty()) {
            return emptyList();
        }
        return requests.stream().map(RequestMapper::entityToDto).collect(toList());
    }
}
