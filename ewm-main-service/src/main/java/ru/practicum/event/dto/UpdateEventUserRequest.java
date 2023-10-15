package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.model.EventStateActionUser;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateEventUserRequest {

    @Length(min = 20, max = 2000, message = "Текст аннотации должен быть от 20 до 2000 символов")
    private String annotation;

    @Positive
    private Long category;

    @Length(min = 20, max = 7000, message = "Текст описания должен быть от 20 до 7000 символов")
    private String description;

    @FutureOrPresent(message = "Дата события должна быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;
    private Boolean requestModeration;

    private EventStateActionUser stateAction;

    @Length(min = 3, max = 120, message = "Текст заголовка должен быть от 3 до 120 символов")
    private String title;
}
