package ru.practicum.event.repository;

import ru.practicum.event.model.Event;
import ru.practicum.event.dto.EventFilterParameters;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


public class EventCustomRepositoryImpl implements EventCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Event> findAllEventsByParameters(EventFilterParameters parameters) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);

        Predicate criteria = buildCriteria(criteriaBuilder, eventRoot, parameters);

        criteriaQuery.select(eventRoot).where(criteria);

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(Math.toIntExact(parameters.getFrom()))
                .setMaxResults(Math.toIntExact(parameters.getSize()))
                .getResultList();
    }

    private Predicate buildCriteria(CriteriaBuilder criteriaBuilder,
                                    Root<Event> eventRoot,
                                    EventFilterParameters parameters) {

        Predicate criteria = criteriaBuilder.conjunction();

        LocalDateTime rangeStart = parameters.getRangeStart();
        LocalDateTime rangeEnd = parameters.getRangeEnd();

        if (rangeStart == null && rangeEnd == null) {
            criteria = criteriaBuilder.and(criteria,
                    criteriaBuilder.greaterThanOrEqualTo(eventRoot.get("eventDate"),
                            LocalDateTime.now()));
        } else {
            if (rangeStart != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.greaterThanOrEqualTo(eventRoot.get("eventDate"),
                                rangeStart));
            }

            if (rangeEnd != null) {
                criteria = criteriaBuilder.and(criteria,
                        criteriaBuilder.lessThanOrEqualTo(eventRoot.get("eventDate"),
                                rangeEnd));
            }
        }

        if (Objects.nonNull(parameters.getPaid())) {
            criteriaBuilder.and(criteria, criteriaBuilder.equal(eventRoot.get("paid"), parameters.getPaid()));
        }

        if (Objects.nonNull(parameters.getUserIds())) {
            criteriaBuilder.and(criteria, criteriaBuilder.equal(eventRoot.get("initiator"), parameters.getUserIds()));
        }

        Boolean paid = parameters.getPaid();
        if (paid != null) {
            criteria = criteriaBuilder.and(criteria, eventRoot.get("paid").in(paid));
        }

        String text = parameters.getText();
        if (text != null && !text.isBlank()) {
            Predicate annotation = criteriaBuilder.like(
                    criteriaBuilder.lower(eventRoot.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate description = criteriaBuilder.like(
                    criteriaBuilder.lower(eventRoot.get("description")), "%" + text.toLowerCase() + "%");
            criteria = criteriaBuilder.and(criteria, criteriaBuilder.or(annotation, description));
        }

        if (parameters.getCategories() != null) {
            List<Long> categories = parameters.getCategories();
            if (categories != null && !categories.isEmpty()) {
                criteria = criteriaBuilder.and(criteria, eventRoot.get("category").in(categories));
            }
        }

        if (parameters.getEventState() != null && parameters.getEventState().size() == 1) {
            criteria = criteriaBuilder.and(criteria, eventRoot.get("eventState").in(parameters.getEventState().get(0)));
        }

        return criteria;
    }
}
