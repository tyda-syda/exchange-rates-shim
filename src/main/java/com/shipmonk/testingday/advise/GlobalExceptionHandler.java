package com.shipmonk.testingday.advise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.shipmonk.testingday.exceptions.MalformedRequestException;
import com.shipmonk.testingday.exceptions.ProviderRateException;
import com.shipmonk.testingday.exceptions.ProviderRateNotFoundException;
import com.shipmonk.testingday.exceptions.RatesNotFoundException;
import com.shipmonk.testingday.model.ErrorResponse;
import com.shipmonk.testingday.model.ErrorType;
import com.shipmonk.testingday.model.Response;
import com.shipmonk.testingday.utils.Consts;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Response> handleDateTimeParse(DateTimeParseException ex) {
        return respond(
            HttpStatus.BAD_REQUEST,
            "Invalid date '%s': expected date in format %s".formatted(ex.getParsedString(), Consts.DATE_FORMAT),
            ex,
            ErrorType.BAD_REQUEST
        );
    }

    @ExceptionHandler(MalformedRequestException.class)
    public ResponseEntity<Response> handleMalformedRequest(MalformedRequestException ex) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), ex, ErrorType.BAD_REQUEST);
    }

    @ExceptionHandler(RatesNotFoundException.class)
    public ResponseEntity<Response> handleRateNotFound(RatesNotFoundException ex) {
        return respond(
            HttpStatus.NOT_FOUND,
            "Rates are bot available",
            ex,
            ErrorType.INTERNAL
        );
    }

    @ExceptionHandler(ProviderRateException.class)
    public ResponseEntity<Response> handleProviderRate(ProviderRateException ex) {
        return respond(
            HttpStatus.BAD_GATEWAY,
            "Rates provider is currently unavailable",
            ex,
            ErrorType.INTERNAL
        );
    }

    @ExceptionHandler(ProviderRateNotFoundException.class)
    public ResponseEntity<Response> handleProviderRateNotFound(ProviderRateNotFoundException ex) {
        return respond(
            HttpStatus.BAD_GATEWAY,
            ex.getMessage(),
            ex,
            ErrorType.INTERNAL
        );
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Response> handleThrowable(Throwable throwable) {
        return respond(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            throwable,
            ErrorType.INTERNAL
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        logError(statusCode, ex);

        return ResponseEntity.status(statusCode)
            .headers(headers)
            .body(buildResponse(resolveMessage(ex, statusCode), ErrorType.INTERNAL));
    }

    private String resolveMessage(Exception ex, HttpStatusCode statusCode) {
        if (ex instanceof NoResourceFoundException e) {
            return "No endpoint found for %s %s".formatted(e.getHttpMethod(), e.getResourcePath());
        }
        if (ex instanceof NoHandlerFoundException e) {
            return "No endpoint found for %s %s".formatted(e.getHttpMethod(), e.getRequestURL());
        }
        if (ex instanceof HttpRequestMethodNotSupportedException e) {
            return "HTTP method %s is not supported for this endpoint, supported methods: %s"
                .formatted(e.getMethod(), e.getSupportedHttpMethods());
        }
        if (ex instanceof HttpMediaTypeNotSupportedException e) {
            return "Content type '%s' is not supported, supported types: %s"
                .formatted(e.getContentType(), e.getSupportedMediaTypes());
        }
        if (ex instanceof HttpMediaTypeNotAcceptableException e) {
            return "Requested media type is not acceptable, supported types: %s"
                .formatted(e.getSupportedMediaTypes());
        }
        if (ex instanceof MissingPathVariableException e) {
            return "Missing path variable '%s'".formatted(e.getVariableName());
        }
        if (ex instanceof MissingServletRequestParameterException e) {
            return "Missing required parameter '%s' of type %s"
                .formatted(e.getParameterName(), e.getParameterType());
        }
        if (ex instanceof TypeMismatchException e) {
            return typeMismatchMessage(e);
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Malformed or unreadable request body";
        }
        if (ex instanceof MethodArgumentNotValidException e) {
            return "Validation failed: " + e.getBindingResult().getFieldErrors().stream()
                .map(fe -> "'%s' %s".formatted(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        }

        return statusCode.is5xxServerError()
            ? "Internal server error"
            : Optional.ofNullable(HttpStatus.resolve(statusCode.value()))
                .map(HttpStatus::getReasonPhrase)
                .orElse("Request failed");
    }

    private String typeMismatchMessage(TypeMismatchException ex) {
        String name = ex.getPropertyName();
        Object value = ex.getValue();
        Class<?> type = ex.getRequiredType();

        String expected;

        if (type == LocalDate.class) {
            expected = "date in format " + Consts.DATE_FORMAT;
        } else if (type != null && type.isEnum()) {
            expected = "one of " + Arrays.toString(type.getEnumConstants());
        } else {
            expected = "value of type " + (type != null ? type.getSimpleName() : "unknown");
        }

        return "Invalid value '%s' for parameter '%s': expected %s".formatted(value, name, expected);
    }

    private ResponseEntity<Response> respond(
        HttpStatus status,
        String message,
        Throwable ex,
        ErrorType errorType
    ) {
        logError(status, ex);
        return ResponseEntity.status(status).body(buildResponse(message, errorType));
    }

    private Response buildResponse(String message, ErrorType errorType) {
        return Response.builder()
            .success(false)
            .timestamp(LocalDateTime.now())
            .error(
                ErrorResponse.builder()
                    .code(errorType.getCode())
                    .type(errorType.getType())
                    .message(message)
                    .build()
            )
            .build();
    }

    private void logError(HttpStatusCode status, Throwable ex) {
        if (status.is5xxServerError()) {
            log.error("Request failed with status [{}]", status.value(), ex);
        } else {
            log.warn("Request rejected with status [{}]: {}", status.value(), ex.getMessage());
        }
    }

}
