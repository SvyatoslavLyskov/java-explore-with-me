package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.utils.ObjectMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = ObjectMapper.toCategory(dto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Создана категория с id {}.", savedCategory.getId());
        return ObjectMapper.toCategoryDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {
        Category oldCategory = getCategoryById(categoryId);
        oldCategory.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(oldCategory);
        log.info("Категория с id {} успешно обновлена.", categoryId);
        return ObjectMapper.toCategoryDto(updatedCategory);
    }

    @Transactional
    public void removeCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория", id);
        }
        List<Event> events = eventRepository.findFirstByCategoryId(id);
        if (!events.isEmpty()) {
            throw new ConflictException("Категория содержит мероприятия.");
        }
        categoryRepository.deleteById(id);
        log.info("Категория с id {} удалена.", id);
    }

    public List<CategoryDto> findAllCategories(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        Page<Category> categories = categoryRepository.findAll(pageRequest);
        log.info("Получен список категорий, всего: {} шт.", categories.getTotalElements());
        return ObjectMapper.toCategoryDtoList(categories);
    }

    public CategoryDto findCategoryById(Long categoryId) {
        Category category = getCategoryById(categoryId);
        log.info("Найдена категория {}.", categoryId);
        return ObjectMapper.toCategoryDto(category);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория", categoryId));
    }
}