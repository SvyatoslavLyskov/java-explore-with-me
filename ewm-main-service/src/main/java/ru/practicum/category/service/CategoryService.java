package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.utils.CategoryMapper;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = CategoryMapper.toCategory(dto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Создана новая категория {}.", savedCategory.getId());
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {
        Category oldCategory = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Категория", categoryId));
        oldCategory.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(oldCategory);
        log.info("Категория {} обновлена.", categoryId);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }

    @Transactional
    public void removeCategoryById(Long id) {
        checkCategoryAvailability(categoryRepository, id);
        Event event = eventRepository.findFirstByCategoryId(id);
        if (event != null) {
            throw new ConflictException("Категория содержит события.");
        }
        categoryRepository.deleteById(id);
        log.info("Категория {} удалена.", id);
    }

    public List<CategoryDto> findAllCategories(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        Page<Category> categories = categoryRepository.findAll(pageRequest);
        log.info("Список категорий получен, всего: {}.", categories.getTotalElements());
        return CategoryMapper.toCategoryDtoList(categories);
    }

    public CategoryDto findCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Категория", categoryId));
        log.info("Найдена категория {}.", categoryId);

        return CategoryMapper.toCategoryDto(category);
    }

    private static void checkCategoryAvailability(CategoryRepository repository, Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Категория", id);
        }
    }
}
