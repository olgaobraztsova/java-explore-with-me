package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    /**
     * Добавление новой подборки (подборка может не содержать событий)
     */
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {

        List<Event> events = eventRepository.findByIdIn(newCompilationDto.getEvents());
        Compilation compilation = compilationRepository.save(CompilationMapper.dtoToEntity(newCompilationDto, events));
        return CompilationMapper.toCompilationDto(compilation);
    }

    /**
     * Удаление подборки
     */
    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборки с id = " + compId + " не существует", "Object not found"));
        compilationRepository.delete(compilation);
    }

    /**
     * Обновить информацию о подборке
     */
    @Override
    public CompilationDto editCompilationById(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборки с id = " + compId + " не существует", "Object not found"));

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findByIdIn(updateCompilationRequest.getEvents());

            compilation.setEvents(events);
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}
