package com.example.demo.exception;

import com.example.demo.exception.authorize.*;
import com.example.demo.exception.cart.EmptyException;
import com.example.demo.exception.purchased.InsufficientFundsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String getReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? referer : "/";
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public String handleNotAuthorized(NotAuthorizedUserException e,
                                      HttpServletRequest request,
                                      RedirectAttributes ra) {
        ra.addFlashAttribute("notAuthorizedUserExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(EmptyException.class)
    public String handleEmpty(EmptyException e,
                              HttpServletRequest request,
                              RedirectAttributes ra) {
        ra.addFlashAttribute("emptyCartExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFunds(InsufficientFundsException e,
                                          HttpServletRequest request,
                                          RedirectAttributes ra) {
        ra.addFlashAttribute("insufficientFundsExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(PasswordsMismatchException.class)
    public String handlePasswordsMismatch(PasswordsMismatchException e,
                                          HttpServletRequest request,
                                          RedirectAttributes ra) {
        ra.addFlashAttribute("passwordsMismatchExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public String handleAlreadyRegistered(AlreadyRegisteredException e,
                                          HttpServletRequest request,
                                          RedirectAttributes ra) {
        ra.addFlashAttribute("alreadyRegisteredExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public String handleInvalidPassword(InvalidPasswordException e,
                                        HttpServletRequest request,
                                        RedirectAttributes ra) {
        ra.addFlashAttribute("invalidPasswordExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException e,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {
        ra.addFlashAttribute("userNotFoundExceptionMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException e,
                                       HttpServletRequest request,
                                       RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:" + getReferer(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        return "redirect:" + getReferer(request);
    }
}