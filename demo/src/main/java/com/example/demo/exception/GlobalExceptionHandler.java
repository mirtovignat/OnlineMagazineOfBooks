package com.example.demo.exception;

import com.example.demo.exception.cart.EmptyException;
import com.example.demo.exception.purchased.InsufficientFundsException;
import com.example.demo.exception.user.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
        String requestedWith = httpServletRequest.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    private String getReferer(HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? referer : "/";
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public Object handleNotAuthorized(NotAuthorizedUserException e,
                                      HttpServletRequest httpServletRequest,
                                      RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(EmptyException.class)
    public Object handleEmpty(EmptyException e,
                              HttpServletRequest httpServletRequest,
                              RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("emptyCartExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Object handleInsufficientFunds(InsufficientFundsException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("insufficientFundsExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(PasswordsMismatchException.class)
    public Object handlePasswordsMismatch(PasswordsMismatchException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("passwordsMismatchExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public Object handleAlreadyRegistered(AlreadyRegisteredException e,
                                          HttpServletRequest httpServletRequest,
                                          RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public Object handleInvalidPassword(InvalidPasswordException e,
                                        HttpServletRequest httpServletRequest,
                                        RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("invalidPasswordExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(UserNotFoundException e,
                                     HttpServletRequest httpServletRequest,
                                     RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("userNotFoundExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(DataCoincidenceException.class)
    public Object handleDataCoincidence(DataCoincidenceException e,
                                        HttpServletRequest httpServletRequest,
                                        RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("infoMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFound(EntityNotFoundException e,
                                       HttpServletRequest httpServletRequest,
                                       RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception e,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {
        if (isAjaxRequest(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"Произошла ошибка: " + e.getMessage() + "\"}");
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        return "redirect:" + getReferer(httpServletRequest);
    }
}