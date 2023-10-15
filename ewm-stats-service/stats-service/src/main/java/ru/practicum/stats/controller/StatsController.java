package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.service.StatsService;

import javax.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<EndPointHitDto> createHit(@RequestBody @Valid EndPointHitDto endPointHitDto) {
        return new ResponseEntity<>(statsService.createHit(endPointHitDto), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam(name = "start") String startDate,
            @RequestParam(name = "end") String endDate,
            @RequestParam(name = "uris", required = false, defaultValue = "") List<String> uris,
            @RequestParam(name = "unique", required = false, defaultValue = "false") Boolean unique) {

        LocalDateTime start = LocalDateTime.parse(
                URLDecoder.decode(startDate, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(
                URLDecoder.decode(endDate, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return statsService.getStats(start, end, uris, unique);
    }

    @GetMapping("/views/{eventId}")
    public int getView(@PathVariable long eventId) {
        return statsService.getViews(eventId);
    }
}

