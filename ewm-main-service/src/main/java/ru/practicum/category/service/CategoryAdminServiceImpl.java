package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.repository.EventRepository;


@Service
@RequiredArgsConstructor
@Transactional
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.findByName(newCategoryDto.getName()) != null) {
            throw new ConflictException(
                    "Категория с названием " + newCategoryDto.getName() + " уже существует.",
                    "Integrity constraint has been violated.");
        }
        Category savedCategory = categoryRepository.save(CategoryMapper.dtoNewToEntity(newCategoryDto));
        return CategoryMapper.entityToDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категории с ID = " + catId + " не существует",
                        "Object not found"));

        if (categoryDto.getName().equals(existingCategory.getName())) {
            return CategoryMapper.entityToDto(existingCategory);
        }

        if (categoryRepository.findByNameAndIdNot(categoryDto.getName(), catId) != null) {
            throw new ConflictException("Категория с названием " + categoryDto.getName() + " уже существует.",
                    "Integrity constraint has been violated.");
        }

        Category updatedCategory = categoryRepository.save(CategoryMapper.dtoToCategory(categoryDto));

        return CategoryMapper.entityToDto(updatedCategory);
    }

    @Override
    public void deleteCategoryById(Long catId) {

        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с ID = " + catId + " не найдена",
                        "Object not found"));

        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("Категория не может быть удалена, т.к. к ней привязаны события.",
                    "Integrity constraint has been violated.");
        }

        categoryRepository.deleteById(catId);
    }
}
