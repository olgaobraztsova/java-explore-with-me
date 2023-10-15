package ru.practicum.errors.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConflictException extends RuntimeException {
    private String reason;

    public ConflictException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
}
