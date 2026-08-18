package com.example.demo.exception;

import com.example.demo.web.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private String fallbackPath(HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? referer : "/";
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException businessException,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        String message = businessException.getMessage();
        ErrorCode errorCode = businessException.getErrorCode();
        if (RequestUtils.isAjaxRequest(httpServletRequest)) {
            HttpStatus httpStatus = switch (errorCode) {
                case USER_NOT_FOUND, ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
                case PASSWORD_INVALID, NOT_AUTHORIZED -> HttpStatus.UNAUTHORIZED;
                case INSUFFICIENT_FUNDS -> HttpStatus.PAYMENT_REQUIRED;
                case ALREADY_REGISTERED, DATA_COINCIDENCE, ALREADY_TAKEN -> HttpStatus.CONFLICT;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity.status(httpStatus).body(Map.of("message", message));
        }
        if (errorCode == ErrorCode.PASSWORD_INVALID) {
            redirectAttributes.addFlashAttribute("invalidPasswordExceptionMessage", message);
        } else if (errorCode == ErrorCode.USER_NOT_FOUND) {
            redirectAttributes.addFlashAttribute("userNotFoundExceptionMessage", message);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
        return "redirect:" + fallbackPath(httpServletRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(MethodArgumentNotValidException methodArgumentNotValidException,
                                            HttpServletRequest httpServletRequest,
                                            RedirectAttributes redirectAttributes) {
        String message = methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.VALIDATION_ERROR.getMessage());

        if (RequestUtils.isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + fallbackPath(httpServletRequest);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public Object handleMissingSessionAttribute(HttpServletRequest httpServletRequest,
                                                RedirectAttributes redirectAttributes) {
        String message = ErrorCode.NOT_AUTHORIZED.getMessage();
        if (com.example.demo.web.util.RequestUtils.isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/login";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception exception,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {
        String userMessage = ErrorCode.ERROR.getMessage();
        if (RequestUtils.isAjaxRequest(httpServletRequest)) {
            log.error("Ошибка при AJAX запросе", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", userMessage));
        }
        String referer = httpServletRequest.getHeader("Referer");
        String currentUri = httpServletRequest.getRequestURI();
        if (referer == null || referer.isBlank() || referer.contains(currentUri)) {
            throw new RuntimeException(exception);
        }
        redirectAttributes.addFlashAttribute("errorMessage", userMessage);
        return "redirect:" + referer;
    }
}