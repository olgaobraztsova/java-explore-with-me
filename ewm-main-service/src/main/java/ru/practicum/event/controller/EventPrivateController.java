package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventPrivateService;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createNewEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventPrivateService.createNewEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getAllUserEvents(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        return eventPrivateService.getAllUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getCurrentUserEventByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getCurrentUserEventByEventId(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getRequestsByEventIdByCurrentUser(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return eventPrivateService.getRequestsByEventIdByCurrentUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult editRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestUpdate) {
        return eventPrivateService.editRequestStatus(userId, eventId, eventRequestUpdate);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto editCurrentUserEventByEventId(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return eventPrivateService.editCurrentUserEventByEventId(userId, eventId, updateEventUserRequest);
    }
}
