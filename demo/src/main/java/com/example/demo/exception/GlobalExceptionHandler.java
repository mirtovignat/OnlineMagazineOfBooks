package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private String fallbackPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return "/";
        }
        if (uri.startsWith("/register")) {
            return "/register";
        }
        if (uri.startsWith("/login")) {
            return "/login";
        }
        if (uri.startsWith("/cart")) {
            return "/cart";
        }
        if (uri.startsWith("/favourites")) {
            return "/favourites";
        }
        return "/";
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException businessException,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        String message = businessException.getMessage();
        ErrorCode errorCode = businessException.getErrorCode();

        if (isAjax(httpServletRequest)) {
            HttpStatus httpStatus = switch (errorCode) {
                case USER_NOT_FOUND, ENTITY_NOT_FOUND -> HttpStatus.NOT_FOUND;
                case PASSWORD_INVALID, NOT_AUTHORIZED -> HttpStatus.UNAUTHORIZED;
                case INSUFFICIENT_FUNDS -> HttpStatus.PAYMENT_REQUIRED;
                case ALREADY_REGISTERED, DATA_COINCIDENCE -> HttpStatus.CONFLICT;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity.status(httpStatus).body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + fallbackPath(httpServletRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Ошибка валидации");
        if (isAjax(request)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + fallbackPath(request);
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception exception,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {
        if (isAjax(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Внутренняя ошибка: " + exception.getMessage()));
        }
        redirectAttributes.addFlashAttribute("errorMessage",
                "Произошла ошибка: " + exception.getMessage());
        return "redirect:" + fallbackPath(httpServletRequest);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public Object handleMissingSessionAttribute(ServletRequestBindingException servletRequestBindingException,
                                                HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {
        String message = "Авторизуйтесь!";
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", message));
        }
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/login";
    }
}