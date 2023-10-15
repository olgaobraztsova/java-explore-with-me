package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {
    @Size(min = 1, max = 50, message = "Размер заголовка должен составлять от 1 до 50 символов")
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
    private Boolean pinned = false;
    private List<Long> events = new ArrayList<>();
}