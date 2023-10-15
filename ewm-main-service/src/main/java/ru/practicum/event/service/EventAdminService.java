package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventAdminService {
    List<EventFullDto> searchEvents(List<Long> users,
                                    List<EventState> states,
                                    List<Long> categories,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    long from,
                                    long size);

    EventFullDto editEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);
}
