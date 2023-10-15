package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.EventFilterParameters;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.enums.EventSort;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.RequestsCount;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.statClient.StatClientCommunicator;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatClientCommunicator statClientCommunicator;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, EventSort eventSort,
                                         long from, long size, HttpServletRequest request) {

        /**
         * это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
         * текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
         * если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события,
         которые произойдут позже текущей даты и времени
         * информация о каждом событии должна включать в себя количество просмотров и количество уже
         одобренных заявок на участие
         * информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить
         в сервисе статистики
         * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
         * */

        if (rangeEnd != null && rangeStart != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadParameterException("Время окончания не может быть раньше времени начала.",
                    "Incorrectly made request.");
        }


        EventFilterParameters eventFilterParameters =
                EventFilterParameters.builder()
                        .categories(categories)
                        .text(text)
                        .paid(paid)
                        .rangeStart(rangeStart)
                        .rangeEnd(rangeEnd)
                        .onlyAvailable(onlyAvailable)
                        .eventState(List.of(EventState.PUBLISHED))
                        .sort(eventSort)
                        .from(from)
                        .size(size)
                        .build();

        List<Event> eventsResult = eventRepository.findAllEventsByParameters(eventFilterParameters);

        if (eventsResult.isEmpty()) {
            return List.of();
        }

        List<Long> eventIdList = eventsResult.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        // get confirmed requests
        List<RequestsCount> confirmedRequests = requestRepository.getNumberOfConfirmedRequestsForEvents(eventIdList);
        Map<Long, Long> confirmedRequestsMap = new HashMap<>();

        for (RequestsCount requestsCount : confirmedRequests) {
            if (confirmedRequestsMap.containsKey(requestsCount.getEventId())) {
                long count = confirmedRequestsMap.get(requestsCount.getEventId());
                confirmedRequestsMap.put(requestsCount.getEventId(), count + requestsCount.getRequestCount());
            } else {
                confirmedRequestsMap.put(requestsCount.getEventId(), requestsCount.getRequestCount());
            }
        }

        // get views
        Map<Long, Long> views = statClientCommunicator.getViews(eventsResult);

        List<EventShortDto> eventShortDtoList = new ArrayList<>();

        for (Event event : eventsResult) {
            EventShortDto eventShortDto = EventMapper.toEventDtoShort(event);
            eventShortDto.setViews(views.get(event.getId()));
            eventShortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
            eventShortDtoList.add(eventShortDto);
        }

        statClientCommunicator.postStat("ewm-main-service", request);

        return eventShortDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getById(Long eventId, HttpServletRequest request) {

        /**
         * Событие должно быть опубликовано
         * информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
         * информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в
         сервисе статистики
         * В случае, если события с заданным id не найдено, возвращает статус код 404
         * */
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with ID = " + eventId + " was not found",
                        "The required object was not found"));

        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано", "The required object is not published or was not found");
        }

        List<RequestsCount> confirmedRequests = requestRepository.getNumberOfConfirmedRequestsForEvents(List.of(event.getId()));

        Long views = statClientCommunicator.getViews(List.of(event)).getOrDefault(eventId, 0L);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(
                event,
                confirmedRequests.isEmpty() ? 0 : confirmedRequests.get(0).getRequestCount(),
                views);

        statClientCommunicator.postStat("ewm-main-service", request);

        return eventFullDto;
    }
}
