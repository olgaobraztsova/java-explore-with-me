package ru.practicum.compilation.mapper;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;


import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class CompilationMapper {
    public static Compilation dtoToEntity(NewCompilationDto newCompilationDto, List<Event> events) {
        return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events == null ? Collections.emptyList() : events)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(EventMapper.entityListToShortDto(compilation.getEvents()))
                .build();
    }

    public static List<CompilationDto> entityListToDto(List<Compilation> compilations) {
        if (compilations.isEmpty()) {
            return emptyList();
        }

        return compilations.stream().map(CompilationMapper::toCompilationDto).collect(toList());
    }
}
