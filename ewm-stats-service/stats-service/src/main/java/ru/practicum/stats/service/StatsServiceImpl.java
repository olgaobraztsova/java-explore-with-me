package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.errors.exceptions.BadParameterException;
import ru.practicum.stats.mapper.EndPointHitMapper;
import ru.practicum.stats.mapper.ViewStatsMapper;
import ru.practicum.stats.model.EndPointHit;
import ru.practicum.stats.repository.StatsRepository;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@RequestMapping(path = "/")
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start.isAfter(end)) {
            throw new BadParameterException("Дата начала не может быть раньше даты окончания", "bad parameter - stats");
        }

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

    @Override
    @Transactional(readOnly = true)
    public int getViews(long eventId) {
        return statsRepository.countDistinctViews("/events/" + eventId);
    }
}
