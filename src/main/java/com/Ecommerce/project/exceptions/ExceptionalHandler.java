package com.Ecommerce.project.exceptions;


import com.Ecommerce.project.payload.APIResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionalHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException manve) {
        Map<String, String> response = new HashMap<>();
        manve.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError)err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> myResourceNotFoundException(ResourceNotFoundException rnfe) {
        String message = rnfe.getMessage();
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException apiExceptions) {
        String message = apiExceptions.getMessage();
        APIResponse exception = new APIResponse(message, false);
        return new ResponseEntity<>(exception, HttpStatus.BAD_REQUEST);

    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> myException(Exception exception) {
//        String message = "Something is wrong!";
//        System.out.println(exception);
//        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
//
//    }

}
