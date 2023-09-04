package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationInputDto;
import ru.practicum.compilation.dto.CompilationOutputDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationOutputDto createCompilation(@Validated(Create.class) @RequestBody CompilationInputDto dto) {
        return compilationService.createCompilation(dto);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationOutputDto updateCompilation(@PathVariable("compId") Long compId,
                                                  @Validated(Update.class) @RequestBody CompilationInputDto compilationDto) {
        return compilationService.updateCompilation(compilationDto, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCompilationById(@PathVariable Long compId) {
        compilationService.removeCompilationById(compId);
    }
}
