package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static ru.practicum.request.model.RequestStatus.CONFIRMED;
import static ru.practicum.request.model.RequestStatus.PENDING;


@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    /**
     * Получение информации о заявках текущего пользователя на участие в чужих событиях
     */
    @Override
    public List<ParticipationRequestDto> getEventRequestsByRequester(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with ID = " + userId + " doesn't exist", "Object not found"));

        return RequestMapper.entityListToDto(requestRepository.findAllByRequesterId(userId));
    }

    /**
     * Добавление запроса от текущего пользователя на участие в событии
     */
    @Override
    public ParticipationRequestDto createEventRequest(Long userId, Long eventId) {

        if (eventId == null) {
            throw new BadParameterException(
                    "Указание события обязательно при создании запроса на участие", "Bad Parameter");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id = " + eventId + " doesn't exist", "The required object was not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        //инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Пользователь не может участвовать в собственном событии", "Access Denied");
        }

        // нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Participation can be requested only in PUBLISHED events",
                    "For the requested operation the conditions are not met.");
        }

        //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
        Long confirmedRequests = requestRepository.getNumberOfConfirmedRequestsByEventId(eventId);
        if (event.getParticipantLimit() > 0 && Objects.equals(event.getParticipantLimit(), confirmedRequests)) {
            throw new ConflictException("Participants limit has been exceeded", "access denied.");
        }

        //нельзя добавить повторный запрос (Ожидается код ошибки 409)
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException(
                    "Request for Event ID=" + eventId + " from User with ID=" + userId + " already exists",
                    "Duplicate data");
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .build();

        //если для события отключена пре-модерация запросов на участие, то запрос должен автоматически
        // перейти в состояние подтвержденного
        if (event.getRequestModeration() && event.getParticipantLimit() > 0) {
            request.setStatus(PENDING);
        } else {
            request.setStatus(CONFIRMED);
        }

        return RequestMapper.entityToDto(requestRepository.save(request));
    }

    /**
     * Отмена своего запроса на участие в событии
     */
    @Override
    public ParticipationRequestDto cancelEventRequest(Long userId, Long requestId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id = " + userId + " doesn't exist", "object not found"));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found", "Object not found"));

        if (!request.getRequester().equals(user)) {
            throw new ConflictException(
                    "Запрос с ID=" + requestId + " не принадлежит пользователю с ID=" + userId,
                    "Access denied");
        }
        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.entityToDto(requestRepository.save(request));
    }
}
