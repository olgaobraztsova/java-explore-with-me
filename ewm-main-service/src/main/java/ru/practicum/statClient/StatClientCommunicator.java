package ru.practicum.statClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.event.model.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@Service
@RequiredArgsConstructor
public class StatClientCommunicator {

    private final StatsClient statsClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<Long, Long> getViews(List<Event> events) {

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime startDate = LocalDateTime.now();

        for (Event event : events) {
            if (startDate.isAfter(event.getCreatedOn())) {
                startDate = event.getCreatedOn();
            }
        }

        ResponseEntity<Object> response = statsClient.getStats(
                startDate.format(DATE_TIME_FORMATTER),
                LocalDateTime.now().format(DATE_TIME_FORMATTER),
                uris,
                true);

        List<ViewStatsDto> viewStats;

        try {
            String json = mapper.writeValueAsString(response.getBody());
            viewStats = Arrays.asList(mapper.readValue(json, ViewStatsDto[].class));
        } catch (JsonProcessingException e) {
            throw new BadParameterException("Некорректное значение получено из сервера статистики", "bad parameter");
        }

        Map<Long, Long> viewsForEvents = new HashMap<>();

        if (viewStats == null || viewStats.isEmpty()) {
            return viewsForEvents;
        }

        for (ViewStatsDto viewStatsDto : viewStats) {
            String uri = viewStatsDto.getUri();
            String[] split = uri.split("/");
            String id = split[2];
            Long eventId = Long.parseLong(id);
            viewsForEvents.put(eventId, viewStatsDto.getHits());
        }

        return viewsForEvents;
    }


    public void postStat(String app, HttpServletRequest request) {
        EndPointHitDto endPointHitDto = EndPointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .app(app)
                .build();
        statsClient.postStatEvent(endPointHitDto);
    }
}
