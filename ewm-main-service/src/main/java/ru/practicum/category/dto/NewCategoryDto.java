package ru.practicum.category.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NewCategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Length(min = 1, max = 50, message = "Длина названия категории не может быть меньше одного и больше 50 символов.")
    private String name;
}
