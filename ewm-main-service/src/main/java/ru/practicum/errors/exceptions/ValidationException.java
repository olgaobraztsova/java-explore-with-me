package ru.practicum.errors.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationException extends RuntimeException {

    private String reason;

    public ValidationException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
}