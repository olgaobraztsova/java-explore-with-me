package ru.practicum.user.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Поле email не может быть пустым")
    @Email(message = "Поле email - введено некорректное значение")
    @Length(min = 6, max = 254, message = "Некорректная длина email")
    private String email;

    @NotBlank(message = "Поле имени пользователя не может быть пустым")
    @Length(min = 2, max = 250, message = "Некорректная длина имени пользователя")
    private String name;
}
