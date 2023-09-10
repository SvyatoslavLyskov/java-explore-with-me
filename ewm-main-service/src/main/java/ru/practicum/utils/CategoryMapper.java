package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(CategoryDto dto) {
        return new Category(
                dto.getId(),
                dto.getName()
        );
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }

    public List<CategoryDto> toCategoryDtoList(Iterable<Category> categories) {
        List<CategoryDto> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(toCategoryDto(category));
        }
        return result;
    }
}
