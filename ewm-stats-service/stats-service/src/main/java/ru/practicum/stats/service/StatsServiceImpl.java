package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.mapper.EndPointHitMapper;
import ru.practicum.stats.mapper.ViewStatsMapper;
import ru.practicum.stats.model.EndPointHit;
import ru.practicum.stats.repository.StatsRepository;

import org.springframework.transaction.annotation.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@RequestMapping(path = "/")
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    //private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(String startDate, String endDate, List<String> uris, boolean unique) {

        LocalDateTime start = LocalDateTime.parse(
                URLDecoder.decode(startDate, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(
                URLDecoder.decode(endDate, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (uris.isEmpty()) {
            if (unique) {
                log.info("Статистика без URIs, флаг Unique={}", true);
                return ViewStatsMapper.viewStatsDtoList(statsRepository.findHitsByStartAndEndUnique(start, end));
            } else {
                log.info("Статистика без URIs, флаг Unique={}", false);
                return ViewStatsMapper.viewStatsDtoList(
                        statsRepository.findEndPointHitsByStartAndEnd(start, end));
            }
        } else {
            if (unique) {
                log.info("Статистика по URIs: {}, флаг Unique={}", uris, true);
                return ViewStatsMapper.viewStatsDtoList(
                        statsRepository.findHitsByStartAndEndAndByUrisUnique(start, end, uris));
            } else {
                log.info("Статистика по URIs: {}, флаг Unique={}", uris, false);
                return ViewStatsMapper.viewStatsDtoList(
                        statsRepository.findHitsByStartAndEndAndByUris(start, end, uris));
            }
        }
    }

    @Override
    @Transactional
    public EndPointHitDto createHit(EndPointHitDto endPointHitDto) {
        EndPointHit savedEndPointHit = statsRepository.save(EndPointHitMapper.dtoToEntity(endPointHitDto));
        log.info("Сохранен EndpointHit: app - {}, uri - {}.", savedEndPointHit.getApp(), savedEndPointHit.getUri());
        return EndPointHitMapper.entityToDto(savedEndPointHit);
    }
}