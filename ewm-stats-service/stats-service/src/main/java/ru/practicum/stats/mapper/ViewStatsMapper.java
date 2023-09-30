package ru.practicum.stats.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.model.ViewStats;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ViewStatsMapper {
    public static ViewStatsDto entityToDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }

    public List<ViewStatsDto> viewStatsDtoList(List<ViewStats> items) {
        return items.stream()
                .map(ViewStatsMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
