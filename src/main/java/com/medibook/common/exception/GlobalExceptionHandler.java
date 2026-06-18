package com.medibook.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.medibook.common.response.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle Validation DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                "Validation failed", errors);

        return ResponseEntity.badRequest().body(response);
    }

    // Handle RequestParam, PathVariable Validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstrantViolation(ConstraintViolationException ex) {

        ApiResponse<String> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    // Handle bussiness exception
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(BusinessException ex) {

        ApiResponse<String> response = ApiResponse.error(ex.getStatus(), ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    // Handle all other exception
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception
    // ex) {

    // ApiResponse<String> response =
    // ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
    // "Intenal server error");

    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {

        // ex.printStackTrace();

        return ResponseEntity.status(500)
                .body(ApiResponse.error(
                        500,
                        ex.getClass().getName() + ": " + ex.getMessage()));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<String>> handlePropertyReference(PropertyReferenceException ex) {

        ApiResponse<String> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {

        ApiResponse<String> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
