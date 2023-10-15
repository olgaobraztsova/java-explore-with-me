package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestsCount;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.RequestStatusAction;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.statClient.StatClientCommunicator;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.CANCELED;
import static ru.practicum.event.model.EventState.PENDING;

@Service
@Transactional
@RequiredArgsConstructor
public class EventPrivateServiceImpl implements EventPrivateService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatClientCommunicator statClientCommunicator;

    /**
     * Добавление нового события
     */
    @Override
    public EventFullDto createNewEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with ID = " + userId + " doesn't exist", "Object not found"));

        LocalDateTime now = LocalDateTime.now();
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s" + newEventDto.getEventDate(),
                    "For the requested operation the conditions are not met.");
        }

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(
                        "Category with id = " + newEventDto.getCategory() + " was not found",
                        "The required object was not found"));

        Location location = locationRepository.findByLatAndLon(newEventDto.getLocation().getLat(),
                        newEventDto.getLocation().getLat())
                .orElseGet(() -> locationRepository.save(LocationMapper.toEntity(newEventDto.getLocation())));

        Event event = eventRepository.save(EventMapper.toEvent(newEventDto, category, user, location));

        Long confirmedRequests = requestRepository.getNumberOfConfirmedRequestsByEventId(event.getId());
        Long views = statClientCommunicator.getViews(List.of(event)).getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(
                event,
                confirmedRequests,
                views
        );
    }

    /**
     * Получение событий, добавленных текущим пользователем
     */
    @Override
    public List<EventShortDto> getAllUserEvents(Long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with ID = " + userId + " doesn't exist", "Object not found"));

        PageRequest page = PageRequest.of(from / size, size);

        List<Event> eventsResult = eventRepository.findAllByInitiator(user, page);

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
        List<EventShortDto> eventShortDtoList = new ArrayList<>();

        for (Event event : eventsResult) {
            EventShortDto eventShortDto = EventMapper.toEventDtoShort(event);
            eventShortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
            eventShortDto.setViews(views.get(event.getId()));
            eventShortDtoList.add(eventShortDto);
        }

        return eventShortDtoList;
    }

    /**
     * Получение полной информации о событии добавленном текущим пользователем
     */
    @Override
    public EventFullDto getCurrentUserEventByEventId(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "Object not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException("Событие не принадлежит данному пользователю", "Access Denied");
        }

        return EventMapper.toEventFullDto(event,
                requestRepository.getNumberOfConfirmedRequestsByEventId(event.getId()),
                statClientCommunicator.getViews(List.of(event)).getOrDefault(event.getId(), 0L)
        );
    }

    /**
     * Изменение события добавленного текущим пользователем
     */
    @Override
    public EventFullDto editCurrentUserEventByEventId(Long userId,
                                                      Long eventId,
                                                      UpdateEventUserRequest updateEventUserRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не имеет прав редактировать данное событие", "Access Denied");
        }

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed",
                    "For the requested operation the conditions are not met.");
        }

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setEventState(CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setEventState(PENDING);
                    break;
                default:
                    throw new BadParameterException("Недопустимое значение StateActionUser", "Bad Parameter value");
            }
        }

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow(() ->
                    new NotFoundException(
                            "Category with id = " + updateEventUserRequest.getCategory() + " doesn't exist",
                            "The required object was not found")));
        }

        LocalDateTime now = LocalDateTime.now();
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(now.plusHours(2))) {
            throw new ConflictException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s" +
                            updateEventUserRequest.getEventDate(),
                    "For the requested operation the conditions are not met.");
        }

        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.findByLatAndLon(updateEventUserRequest.getLocation().getLat(),
                            updateEventUserRequest.getLocation().getLat())
                    .orElseGet(() -> locationRepository.save(LocationMapper.toEntity(updateEventUserRequest.getLocation())));
            event.setLocation(location);
        }

        Event updatedEvent = editEvent(event, updateEventUserRequest);

        return EventMapper.toEventFullDto(updatedEvent,
                requestRepository.getNumberOfConfirmedRequestsByEventId(event.getId()),
                statClientCommunicator.getViews(List.of(event)).getOrDefault(event.getId(), 0L));

    }

    /**
     * Получение информации о запросах на участие в событии текущего пользователя
     */
    @Override
    public List<ParticipationRequestDto> getRequestsByEventIdByCurrentUser(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не имеет прав редактировать данное событие", "Access Denied");
        }

        return requestRepository.findAllByEvent(event).stream()
                .map(RequestMapper::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     */
    @Override
    public EventRequestStatusUpdateResult editRequestStatus(Long userId,
                                                            Long eventId,
                                                            EventRequestStatusUpdateRequest eventRequestUpdate) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        if (!event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не имеет прав редактировать данное событие", "Access Denied");
        }

        Long confirmedRequests = requestRepository.getNumberOfConfirmedRequestsByEventId(eventId);

        // если для события лимит заявок равен 0 или отключена пре-модерация заявок,
        // то подтверждение заявок не требуется
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение заявок не требуется",
                    "For the requested operation the conditions are not met.");
        }

        // нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
        if (event.getParticipantLimit().equals(confirmedRequests)) {
            throw new ConflictException("The participant limit has been reached",
                    "For the requested operation the conditions are not met.");
        }

        List<Request> requestsToUpdate = requestRepository.findAllByIdIn(eventRequestUpdate.getRequestIds());
        RequestStatusAction newRequestStatus = eventRequestUpdate.getStatus();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        switch (newRequestStatus) {
            case CONFIRMED:

                for (Request request : requestsToUpdate) {
                    if (!request.getStatus().equals(RequestStatus.PENDING)) {
                        //статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
                        throw new ConflictException("Request with ID " + request.getId() + " must have status PENDING",
                                "For the requested operation the conditions are not met.");
                    } else if (event.getParticipantLimit() - confirmedRequests <= 0) {
                        // если при подтверждении данной заявки, лимит заявок для события исчерпан,
                        // то все неподтверждённые заявки необходимо отклонить
                        request.setStatus(RequestStatus.REJECTED);
                    } else {
                        confirmedRequests++;
                        request.setStatus(RequestStatus.CONFIRMED);
                    }
                }
                break;

            case REJECTED:
                for (Request request : requestsToUpdate) {
                    if (!request.getStatus().equals(RequestStatus.PENDING)) {
                        throw new ConflictException("Request with ID " + request.getId() + " must have status PENDING",
                                "For the requested operation the conditions are not met.");
                    }

                    request.setStatus(RequestStatus.REJECTED);
                }
        }

        requestRepository.saveAll(requestsToUpdate);

        List<ParticipationRequestDto> updatedRejectedRequests = new ArrayList<>();
        List<ParticipationRequestDto> updatedConfirmedRequests = new ArrayList<>();

        for (Request request : requestsToUpdate) {
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                updatedRejectedRequests.add(RequestMapper.entityToDto(request));
            } else if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                updatedConfirmedRequests.add(RequestMapper.entityToDto(request));
            }
        }

        result.setRejectedRequests(updatedRejectedRequests);
        result.setConfirmedRequests(updatedConfirmedRequests);

        return result;
    }

    private Event editEvent(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getTitle() != null && !updateEventUserRequest.getTitle().isBlank()) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        if (updateEventUserRequest.getAnnotation() != null && !updateEventUserRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getDescription() != null && !updateEventUserRequest.getDescription().isBlank()) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        return event;
    }
}
