package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationInputDto;
import ru.practicum.compilation.dto.CompilationOutputDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(CompilationInputDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

    public CompilationOutputDto toCompilationOutputDto(Compilation compilation) {
        List<EventShortDto> events = new ArrayList<>();
        if (compilation.getEvents() != null) {
            events = EventMapper.toEventShortDtoList(compilation.getEvents());
        }
        return new CompilationOutputDto(
                compilation.getId(),
                compilation.getTitle(),
                events,
                compilation.getPinned()
        );
    }

    public List<CompilationOutputDto> toCompilationDtoList(Iterable<Compilation> compilations) {
        List<CompilationOutputDto> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            result.add(toCompilationOutputDto(compilation));
        }
        return result;
    }
}
