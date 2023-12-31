package ru.practicum.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.errors.exceptions.BadParameterException;
import ru.practicum.errors.exceptions.ConflictException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(e.getReason())
                .status(HttpStatus.NOT_FOUND)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(e.getReason())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BadParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadParameterException(final BadParameterException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(e.getReason())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }


    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final ConflictException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(e.getReason())
                .status(HttpStatus.CONFLICT)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(RuntimeException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameters(MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .message(e.getMessage())
                .reason(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
