package org.daisleyharrison.security.samples.spring.microservices.identityservice.advisors;

import java.util.List;

import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.OAuth2Error;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.OAuth2ErrorResponse;
import org.daisleyharrison.security.samples.spring.microservices.identityservice.models.OAuth2Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {

        StringBuilder errors = new StringBuilder();
        errors.append("\n");
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            errors.append("Field binding error(s):\n");
            for (FieldError error : fieldErrors) {
                errors.append(String.format("%s: %s\n", error.getField(), error.getDefaultMessage()));
            }
            errors.append("\n");
        }

        List<ObjectError> objectErrors = ex.getBindingResult().getGlobalErrors();
        if (objectErrors.isEmpty()) {
            errors.append("Object binding error(s):\n");
            for (ObjectError error : objectErrors) {
                errors.append(String.format("%s: %s\n", error.getObjectName(), error.getDefaultMessage()));
            }
            errors.append("\n");
        }

        OAuth2ErrorResponse oauth2ErrorResponse = new OAuth2ErrorResponse(OAuth2Error.INVALID_REQUEST,
                ex.getLocalizedMessage() + errors);
        return handleExceptionInternal(ex, oauth2ErrorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<OAuth2ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        OAuth2ErrorResponse oauth2ErrorResponse;
        HttpStatus status;
        if (ex instanceof OAuth2Exception) {
            oauth2ErrorResponse = new OAuth2ErrorResponse((OAuth2Exception) ex);
            status = HttpStatus.BAD_REQUEST;
        } else {
            oauth2ErrorResponse = new OAuth2ErrorResponse(OAuth2Error.INVALID_REQUEST, ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(oauth2ErrorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
            org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, new OAuth2ErrorResponse(OAuth2Error.INVALID_REQUEST, ex), headers,
                HttpStatus.BAD_REQUEST, request);
    }
}