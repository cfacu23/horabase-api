package com.horabase.api.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(
            GlobalExceptionHandler.class
    );

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode status = exception.getStatusCode();
        String message = exception.getReason() == null
                ? "No se pudo completar la solicitud"
                : exception.getReason();

        return ResponseEntity.status(status).body(
                ApiErrorResponse.of(
                        status.value(),
                        reasonPhrase(status),
                        message,
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return validationResponse(
                exception.getBindingResult().getFieldErrors(),
                request
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBinding(
            BindException exception,
            HttpServletRequest request
    ) {
        return validationResponse(
                exception.getBindingResult().getFieldErrors(),
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        return badRequestValidation(errors, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = Map.of(
                exception.getName(),
                "El valor enviado no tiene un formato válido"
        );

        return badRequestValidation(errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "El cuerpo de la solicitud no tiene un formato válido",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "No se encontró el recurso solicitado",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Conflicto de integridad de datos en {}",
                request.getRequestURI(),
                exception
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "La operación entra en conflicto con datos existentes",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Error inesperado al procesar {}",
                request.getRequestURI(),
                exception
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Ocurrió un error interno",
                        request.getRequestURI()
                )
        );
    }

    private ResponseEntity<ApiErrorResponse> validationResponse(
            Iterable<FieldError> fieldErrors,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        fieldErrors.forEach(fieldError -> errors.putIfAbsent(
                fieldError.getField(),
                fieldError.getDefaultMessage() == null
                        ? "El valor no es válido"
                        : fieldError.getDefaultMessage()
        ));

        return badRequestValidation(errors, request);
    }

    private ResponseEntity<ApiErrorResponse> badRequestValidation(
            Map<String, String> errors,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.validation(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Error de validación",
                        request.getRequestURI(),
                        errors
                )
        );
    }

    private String reasonPhrase(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status == null ? "Error" : status.getReasonPhrase();
    }
}
