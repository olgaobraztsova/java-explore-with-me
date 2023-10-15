package ru.practicum.errors.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException {
    private String reason;

    public NotFoundException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
}