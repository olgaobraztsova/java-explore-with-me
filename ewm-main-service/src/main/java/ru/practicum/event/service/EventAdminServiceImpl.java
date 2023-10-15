package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.request.dto.RequestsCount;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.statClient.StatClientCommunicator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.*;


/**
 * Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия.
 * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
 */

@Service
@RequiredArgsConstructor
@Transactional
public class EventAdminServiceImpl implements EventAdminService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatClientCommunicator statClientCommunicator;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<EventFullDto> searchEvents(List<Long> users,
                                           List<EventState> states,
                                           List<Long> categories,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           long from,
                                           long size) {

        if (rangeEnd != null && rangeStart != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadParameterException("Время окончания не может быть раньше времени начала.",
                    "Incorrectly made request.");
        }

        EventFilterParameters eventFilterParameters =
                EventFilterParameters.builder()
                        .userIds(users)
                        .eventState(states)
                        .categories(categories)
                        .rangeStart(rangeStart)
                        .rangeEnd(rangeEnd)
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

        // get eventFullDto
        List<EventFullDto> eventFullDtoList = new ArrayList<>();

        for (Event event : eventsResult) {
            Long eventViews;
            if (views.isEmpty()) {
                eventViews = 0L;
            } else {
                eventViews = views.get(event.getId());
            }
            //Long eventViews = views.get(event.getId());
            Long eventConfirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event, eventConfirmedRequests, eventViews);
            eventFullDtoList.add(eventFullDto);
        }

        return eventFullDtoList;
    }

    @Override
    public EventFullDto editEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));


        // Дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
        LocalDateTime now = LocalDateTime.now();
        if (updateEventAdminRequest.getEventDate() != null && updateEventAdminRequest.getEventDate().isBefore(now.plusHours(1))) {
            throw new ConflictException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                            "Событие не может начинаться ранее часа от текущего времени. Value: %s" +
                            updateEventAdminRequest.getEventDate(),
                    "For the requested operation the conditions are not met.");
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getEventState().equals(EventState.PENDING)) {
                throw new ConflictException("Only pending events can be published or rejected",
                        "For the requested operation the conditions are not met.");
            }

            switch (updateEventAdminRequest.getStateAction()) {
                case REJECT_EVENT:
                    event.setEventState(CANCELED);
                    break;
                case PUBLISH_EVENT:
                    event.setEventState(PUBLISHED);
                    event.setPublishedOn(now);
                    break;
                default:
                    throw new BadParameterException("Недопустимое значение StateActionAdmin", "Bad Parameter value");
            }
        }

        // событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
        // событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory()).orElseThrow(() ->
                    new NotFoundException(
                            "Category with id = " + updateEventAdminRequest.getCategory() + " doesn't exist",
                            "The required object was not found")));
        }

        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationRepository.findByLatAndLon(updateEventAdminRequest.getLocation().getLat(),
                            updateEventAdminRequest.getLocation().getLat())
                    .orElseGet(() -> locationRepository.save(LocationMapper.toEntity(updateEventAdminRequest.getLocation())));
            event.setLocation(location);
        }

        Event updatedEvent = editEvent(event, updateEventAdminRequest);

        return EventMapper.toEventFullDto(eventRepository.save(updatedEvent),
                requestRepository.getNumberOfConfirmedRequestsByEventId(event.getId()),
                statClientCommunicator.getViews(List.of(event)).getOrDefault(event.getId(), 0L));
    }

    private Event editEvent(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getTitle() != null && !updateEventAdminRequest.getTitle().isBlank()) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getAnnotation() != null && !updateEventAdminRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getDescription() != null && !updateEventAdminRequest.getDescription().isBlank()) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        return event;
    }
}
