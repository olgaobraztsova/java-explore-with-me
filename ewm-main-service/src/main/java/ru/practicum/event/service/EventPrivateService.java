package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventPrivateService {
    EventFullDto createNewEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getAllUserEvents(Long userId, int from, int size);

    EventFullDto getCurrentUserEventByEventId(Long userId, Long eventId);

    EventFullDto editCurrentUserEventByEventId(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestsByEventIdByCurrentUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult editRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestUpdate);
}
