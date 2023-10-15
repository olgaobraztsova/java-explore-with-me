package ru.practicum.event.mapper;

import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.model.Location;

public class LocationMapper {

    public static Location toEntity(LocationDto locationDto) {
        return Location.builder()
                .lon(locationDto.getLon())
                .lat(locationDto.getLat())
                .build();
    }

    public static LocationDto toDto(Location location) {
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

}