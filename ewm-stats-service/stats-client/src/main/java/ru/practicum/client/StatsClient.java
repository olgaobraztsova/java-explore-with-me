package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndPointHitDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Service
public class StatsClient extends BaseClient {
    public StatsClient(@Value("${STATS_SERVICE_URL}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postStatEvent(EndPointHitDto endPointHitDto) {
        return post("/hit", endPointHitDto);
    }

    public ResponseEntity<Object> getStats(
            String start, String end, List<String> uris, Boolean unique) {

        Map<String, Object> parameters;

            parameters = Map.of(
                    "start", URLEncoder.encode(start, StandardCharsets.UTF_8),
                    "end", URLEncoder.encode(end, StandardCharsets.UTF_8),
                    "uris", String.join(",", uris),
                    "unique", unique);
            return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);

    }

    public ResponseEntity<Object> getStats(
            String start, String end, Boolean unique) {

        Map<String, Object> parameters;
            parameters = Map.of(
                    "start", URLEncoder.encode(start, StandardCharsets.UTF_8),
                    "end", URLEncoder.encode(end, StandardCharsets.UTF_8),
                    "unique", unique);
            return get("/stats?start={start}&end={end}&unique={unique}", parameters);

    }

    public ResponseEntity<Object> getStats(String start, String end) {

        Map<String, Object> parameters;
        parameters = Map.of(
                "start", URLEncoder.encode(start, StandardCharsets.UTF_8),
                "end", URLEncoder.encode(end, StandardCharsets.UTF_8));
        return get("/stats?start={start}&end={end}", parameters);

    }

    public ResponseEntity<Object> getViews(Long eventId) {
        Map<String, Object> parameters = Map.of(
                "eventId", eventId
        );
        return get("/views/{eventId}", parameters);
    }

}
