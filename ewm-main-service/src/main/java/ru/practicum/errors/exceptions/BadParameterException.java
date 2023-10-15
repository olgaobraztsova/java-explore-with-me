package ru.practicum.errors.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadParameterException extends RuntimeException {
    private String reason;

    public BadParameterException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
}
