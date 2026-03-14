package backend.academy.linktracker.scrapper.exception;

import backend.academy.linktracker.commondto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<@NotNull ApiError> handleInnerException(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ApiError(
                        "",
                        "400",
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        ExceptionUtils.getStackTrace(e).lines().toList()));
    }

    @ExceptionHandler({AbstractException.class})
    public ResponseEntity<@NotNull ApiError> handleMyException(AbstractException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError(
                        e.getDescription(),
                        e.getCode(),
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        ExceptionUtils.getStackTrace(e).lines().toList()));
    }
}
