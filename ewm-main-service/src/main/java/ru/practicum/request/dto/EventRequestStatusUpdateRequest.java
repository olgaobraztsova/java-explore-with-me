package ru.practicum.request.dto;

import lombok.*;
import ru.practicum.request.model.RequestStatusAction;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {

    @NotEmpty
    List<Long> requestIds;

    @NotNull
    RequestStatusAction status;
}
