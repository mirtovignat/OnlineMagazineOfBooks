package com.example.demo.exception;

import com.example.demo.exception.cart.EmptyException;
import com.example.demo.exception.purchased.BalanceLimitExceededException;
import com.example.demo.exception.purchased.InsufficientFundsException;
import com.example.demo.exception.user.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
        return "XMLHttpRequest".equals(httpServletRequest.getHeader("X-Requested-With"));
    }

    private String getReferer(HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? referer : "/";
    }

    private ResponseEntity<Map<String, String>> jsonError(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus).body(Map.of("message", message));
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public Object handleNotAuthorized(NotAuthorizedUserException e,
                                      HttpServletRequest httpServletRequest,
                                      RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(EmptyException.class)
    public Object handleEmpty(EmptyException e,
                              HttpServletRequest httpServletRequest,
                              RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("emptyCartExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Object handleInsufficientFunds(InsufficientFundsException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.PAYMENT_REQUIRED, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("insufficientFundsExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(PasswordsMismatchException.class)
    public Object handlePasswordsMismatch(PasswordsMismatchException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("passwordsMismatchExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public Object handleAlreadyRegistered(AlreadyRegisteredException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.CONFLICT, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public Object handleInvalidPassword(InvalidPasswordException e,
                                        HttpServletRequest httpServletRequest,
                                        RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("invalidPasswordExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(UserNotFoundException e,
                                     HttpServletRequest httpServletRequest,
                                     RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.NOT_FOUND, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("userNotFoundExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(DataCoincidenceException.class)
    public Object handleDataCoincidence(DataCoincidenceException e,
                                        HttpServletRequest httpServletRequest,
                                        RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFound(EntityNotFoundException e,
                                       HttpServletRequest httpServletRequest,
                                       RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.NOT_FOUND, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception e,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла ошибка: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(BalanceLimitExceededException.class)
    public Object handleBalanceLimitExceeded(BalanceLimitExceededException e,
                                             HttpServletRequest httpServletRequest,
                                             RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return jsonError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        redirectAttributes.addFlashAttribute("balanceLimitExceededExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }
}