package ru.practicum.event.repository;

import ru.practicum.event.model.Event;
import ru.practicum.event.dto.EventFilterParameters;

import java.util.List;

public interface EventCustomRepository {
    List<Event> findAllEventsByParameters(EventFilterParameters parameters);

}
