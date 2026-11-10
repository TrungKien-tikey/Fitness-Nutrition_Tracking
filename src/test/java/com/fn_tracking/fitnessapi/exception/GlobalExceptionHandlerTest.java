package com.fn_tracking.fitnessapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeExceptionShouldReturnBadRequestWithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntimeException(new RuntimeException("boom"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("boom", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void validationExceptionShouldReturnFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("registerRequest", "username", "Username cannot be blank"),
                new FieldError("registerRequest", "email", "Email should be valid")
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation Failed", body.get("error"));

        @SuppressWarnings("unchecked")
        Map<String, String> messages = (Map<String, String>) body.get("message");
        assertEquals("Username cannot be blank", messages.get("username"));
        assertEquals("Email should be valid", messages.get("email"));
    }
}
