package ru.practicum.stats.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.stats.model.EndPointHit;

@UtilityClass
public class EndPointHitMapper {
    public static EndPointHit dtoToEntity(EndPointHitDto dto) {
        return EndPointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndPointHitDto entityToDto(EndPointHit endpointHit) {
        return EndPointHitDto.builder()
                .id(endpointHit.getId())
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }
}
