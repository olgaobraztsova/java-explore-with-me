package ru.practicum.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventAndUserOrderByIdAsc(Event event, User user);

    List<Comment> findAllByEvent(Event event);
}
