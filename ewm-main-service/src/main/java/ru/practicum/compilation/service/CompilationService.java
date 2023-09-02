package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitOutputDto;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.dto.CompilationInputDto;
import ru.practicum.compilation.dto.CompilationOutputDto;
import ru.practicum.utils.ObjectMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.utils.StatUtil;
import ru.practicum.utils.UnionService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationService {
    CompilationRepository compilationRepository;
    EventRepository eventRepository;
    UnionService unionService;

    public List<CompilationOutputDto> findAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Compilation> compilations = pinned ?
                compilationRepository.findByPinned(true, pageRequest) :
                compilationRepository.findAll(pageRequest).getContent();
        log.info("Найден список из {} подборок.", compilations.size());
        return compilations.stream()
                .map(this::mapCompilationToDto)
                .collect(Collectors.toList());
    }

    public CompilationOutputDto findCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId);
        log.info("Найдена подборка {}.", compilation.getId());
        return mapCompilationToDto(compilation);
    }

    @Transactional
    public CompilationOutputDto createCompilation(CompilationInputDto dto) {
        Compilation compilation = ObjectMapper.toCompilation(dto);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        if (!dto.getEvents().isEmpty()) {
            compilation.setEvents(eventRepository.findByIdIn(dto.getEvents()));
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Создана новая подборка {}.", savedCompilation.getId());
        return mapCompilationToDto(savedCompilation);
    }


    @Transactional
    public CompilationOutputDto updateCompilation(CompilationInputDto compilationDto, Long compId) {
        Compilation compilation = getCompilation(compId);
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(compilationDto.getEvents()));
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка {} успешно обновлена.", savedCompilation.getTitle());
        return mapCompilationToDto(savedCompilation);
    }

    @Transactional
    public void removeCompilationById(Long compId) {
        checkCompilationAvailability(compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка {} успешно удалена.", compId);
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка", compId));
    }

    private void checkCompilationAvailability(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка", compId);
        }
    }

    private CompilationOutputDto mapCompilationToDto(Compilation compilation) {
        CompilationOutputDto compilationDto = ObjectMapper.toCompilationOutputDto(compilation);
        List<Long> ids = compilationDto.getEvents().stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());
        List<HitOutputDto> hits = unionService.getViews(ids);
        Map<Long, Long> views = StatUtil.mapHitsToViewCountByEventId(hits);
        List<EventShortDto> eventShortDtos = compilationDto.getEvents();
        eventShortDtos.forEach(event -> event.setViews(views.getOrDefault(event.getId(), 0L)));
        compilationDto.setEvents(eventShortDtos);
        return compilationDto;
    }
}