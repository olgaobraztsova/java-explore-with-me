package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static util.Constants.DATE_TIME_PATTERN;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndPointHitDto {

    private Long id;
    @NotBlank
    @Size(max = 255)
    private String app;
    @NotBlank
    @Size(max = 255)
    private String uri;
    @NotBlank
    @Size(max = 16)
    private String ip;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndPointHitDto dto = (EndPointHitDto) o;
        return Objects.equals(id, dto.id) && Objects.equals(app, dto.app) && Objects.equals(uri, dto.uri) && Objects.equals(ip, dto.ip) && Objects.equals(timestamp, dto.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, app, uri, ip, timestamp);
    }
}