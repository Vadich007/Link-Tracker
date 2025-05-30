package backend.academy.bot.controller.http;

import backend.academy.bot.schemas.responses.ApiErrorResponse;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
@ConditionalOnProperty(name = "app.message-transport", havingValue = "http")
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.toString(),
            NoSuchElementException.class.getName(),
            ex.getMessage(),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
