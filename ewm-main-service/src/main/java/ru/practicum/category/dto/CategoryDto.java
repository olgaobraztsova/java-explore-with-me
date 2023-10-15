package ru.practicum.category.dto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории не может быть меньше одного и больше 50 символов.")
    private String name;
    private Long id;
}
