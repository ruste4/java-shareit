package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.item.exceptions.ItemIsNotAvailableException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.UserIsNotBookedItemException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse constraintViolationExceptionHandle(ConstraintViolationException e) {
        if (e.getConstraintName().equals("uq_user_email")) {
            String message = "Email already exist";
            log.warn(message);

            return new ErrorResponse(message);
        }

        log.warn(e.getMessage());

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            UserNotOwnerItemException.class,
            ItemNotFoundException.class,
            BookingNotFound.class,
            BookingAccessBlocked.class,
            BookerIsOwnerItemException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundExceptionHandler(RuntimeException e) {
        log.warn(e.getMessage());

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder stringForLogger = new StringBuilder();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            stringForLogger.append(fieldName + ": " + errorMessage + "; ");
        });

        log.warn(stringForLogger.toString());

        return errors;
    }

    @ExceptionHandler({
            ItemIsNotAvailableException.class,
            BookingIncorrectStartEndDatesException.class,
            BookingAlreadyApprovedException.class,
            UserIsNotBookedItemException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse exceptionHandlerByBadeRequest(RuntimeException e) {
        log.warn(e.getMessage());

        return new ErrorResponse(e.getMessage());
    }

    private static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
