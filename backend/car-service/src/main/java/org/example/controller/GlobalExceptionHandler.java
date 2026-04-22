//package org.example.controller;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.example.dto.ErrorResponse;
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//  // 400
//  @ExceptionHandler ({
//    IllegalArgumentException.class,
//    RuntimeException.class
//  })
//  @ResponseStatus(HttpStatus.BAD_REQUEST)
//  public ErrorResponse handleBadRequest(IllegalArgumentException ex,
//                                        HttpServletRequest request) {
//
//    return new ErrorResponse(
//      LocalDateTime.now(),
//      HttpStatus.BAD_REQUEST.value(),
//      HttpStatus.BAD_REQUEST.getReasonPhrase(),
//      ex.getMessage(),
//      request.getRequestURI()
//    );
//  }
//
//  // 500
//  @ExceptionHandler(Exception.class)
//  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//  public ErrorResponse handleServerError(Exception ex,
//                                         HttpServletRequest request) {
//
//    return new ErrorResponse(
//      LocalDateTime.now(),
//      HttpStatus.INTERNAL_SERVER_ERROR.value(),
//      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//      ex.getMessage(),
//      request.getRequestURI()
//    );
//  }
//
//  //Validation
//  @ExceptionHandler(MethodArgumentNotValidException.class)
//  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
//    String message = ex.getBindingResult().getFieldErrors().stream()
//      .map(error -> error.getField() + ": " + error.getDefaultMessage())
//      .reduce((s1, s2) -> s1 + "; " + s2)
//      .orElse(ex.getMessage());
//
//    ErrorResponse errorResponse = new ErrorResponse(
//      LocalDateTime.now(),
//      HttpStatus.BAD_REQUEST.value(),
//      HttpStatus.BAD_REQUEST.getReasonPhrase(),
//      message,
//      request.getRequestURI()
//    );
//
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//  }
//}
