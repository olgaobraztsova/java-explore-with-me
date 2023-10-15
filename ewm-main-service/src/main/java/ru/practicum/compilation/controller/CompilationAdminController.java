package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationAdminService;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationAdminService compilationAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilations(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return compilationAdminService.createCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationById(@PathVariable Long compId) {
        compilationAdminService.deleteCompilationById(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto editCompilationById(@PathVariable Long compId,
                                              @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        return compilationAdminService.editCompilationById(compId, updateCompilationRequest);
    }

}
