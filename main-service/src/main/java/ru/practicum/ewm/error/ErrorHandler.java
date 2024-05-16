package ru.practicum.ewm.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.CategoryNotEmptyException;
import ru.practicum.ewm.exception.ConflictRequestException;
import ru.practicum.ewm.exception.NotFoundException;

import javax.validation.ValidationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerSQLIntegrityConstraintViolationException(
            final SQLIntegrityConstraintViolationException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("SQLIntegrityConstraintViolationException"), exception.getMessage(),
                HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerValidationException(
            final ValidationException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("ValidationException"), exception.getMessage(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlerNotFoundException(
            final NotFoundException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("NotFoundException"), exception.getMessage(),
                HttpStatus.NOT_FOUND.getReasonPhrase(), HttpStatus.NOT_FOUND.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerConflictRequestException(
            final ConflictRequestException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("ConflictRequestException"), exception.getMessage(),
                HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerBadRequestException(
            final BadRequestException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("BadRequestException"), exception.getMessage(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerCategoryNotEmptyException(
            final CategoryNotEmptyException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("CategoryNotEmptyException"), exception.getMessage(),
                HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerDataIntegrityViolationException(
            final DataIntegrityViolationException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("DataIntegrityViolationException"), exception.getMessage(),
                HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.name());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception) {
        log.info("{}", exception.getMessage());
        return new ApiError(List.of("MethodArgumentNotValidException"), exception.getMessage(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.name());
    }
}
