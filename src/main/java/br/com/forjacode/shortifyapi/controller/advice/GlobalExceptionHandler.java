package br.com.forjacode.shortifyapi.controller.advice;

import br.com.forjacode.shortifyapi.exception.InvalidUrlException;
import br.com.forjacode.shortifyapi.exception.ShortUrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ProblemDetail handleNotFound(ShortUrlNotFoundException e) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ProblemDetail handleInvalidUrl(InvalidUrlException e) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<Map<String, String>> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorMap)
                .toList();

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos");
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    private Map<String, String> toFieldErrorMap(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "message", fieldError.getDefaultMessage() != null
                        ? fieldError.getDefaultMessage()
                        : "valor inválido"
        );
    }
}
