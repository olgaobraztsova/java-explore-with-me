package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.enums.EventSort;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class EventFilterParameters {
    private List<Long> userIds;
    private List<EventState> eventState;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private String text;
    private Boolean paid;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Long from;
    private Long size;
}