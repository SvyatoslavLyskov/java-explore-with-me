package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationInputDto;
import ru.practicum.compilation.dto.CompilationOutputDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.utils.UnionService;
import ru.practicum.utils.CompilationMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final UnionService unionService;

    private static void checkCompilationAvailability(CompilationRepository repository, Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Подборка", id);
        }
    }

    public List<CompilationOutputDto> findAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }
        log.info("Найден список из {} подборок.", compilations.size());
        List<CompilationOutputDto> compilationOutputDtoList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            compilationOutputDtoList.add(mapCompilationToDto(compilation));
        }
        return compilationOutputDtoList;
    }

    public CompilationOutputDto findCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка", compId));
        log.info("Найдена подборка {}.", compilation.getId());
        return mapCompilationToDto(compilation);
    }

    @Transactional
    public CompilationOutputDto createCompilation(CompilationInputDto dto) {
        Compilation compilation = CompilationMapper.toCompilation(dto);
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        } else {
            compilation.setPinned(false);
        }
        if (!dto.getEvents().isEmpty()) {
            compilation.setEvents(eventRepository.findByIdIn(dto.getEvents()));
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Создана новая категория {}.", savedCompilation.getId());
        return mapCompilationToDto(savedCompilation);
    }

    @Transactional
    public CompilationOutputDto updateCompilation(CompilationInputDto compilationDto, Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка", compId));
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null && !compilationDto.getTitle().isBlank()) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(compilationDto.getEvents()));
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Категория {} обновлена.", savedCompilation.getTitle());
        return mapCompilationToDto(savedCompilation);
    }

    @Transactional
    public void removeCompilationById(Long compId) {
        checkCompilationAvailability(compilationRepository, compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка {} удалена.", compId);
    }

    private CompilationOutputDto mapCompilationToDto(Compilation compilation) {
        return CompilationMapper.toCompilationOutputDto(compilation);
    }
}
