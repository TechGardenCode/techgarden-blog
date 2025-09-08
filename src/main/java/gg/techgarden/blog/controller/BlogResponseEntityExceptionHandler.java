package gg.techgarden.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class BlogResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler({HttpStatusCodeException.class})
    ResponseEntity<Object> handleHttpClientErrorException(HttpStatusCodeException ex, WebRequest request) {
        return super.handleExceptionInternal(ex, (Object) null, ex.getResponseHeaders(), ex.getStatusCode(), request);
    }
}