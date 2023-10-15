package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestsCount;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT COUNT(req.id) " +
            "FROM Request AS req " +
            "WHERE req.event.id = ?1 " +
            "AND req.status = 'CONFIRMED' ")
    Long getNumberOfConfirmedRequestsByEventId(Long eventId);

    List<Request> findAllByRequesterId(Long requesterId);

    @Query("SELECT new ru.practicum.request.dto.RequestsCount(req.event.id, COUNT(req.id)) " +
            "FROM Request AS req " +
            "WHERE req.event.id IN ?1 " +
            "AND req.status = 'CONFIRMED' " +
            "GROUP BY req.event.id")
    List<RequestsCount> getNumberOfConfirmedRequestsForEvents(List<Long> eventIdList);

    List<Request> findAllByEvent(Event event);

    List<Request> findAllByIdIn(List<Long> requestId);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);
}
