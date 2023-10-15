package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventCustomRepository {
    List<Event> findAllByCategoryId(Long catId);

    List<Event> findByIdIn(List<Long> events);

    List<Event> findAllByInitiator(User user, PageRequest page);
}
