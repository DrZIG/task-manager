package com.drzig.taskmanager.config;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Invalid request: " + ex.getName() + " was not valid.");
        return "redirect:/";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationError(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Please check your input and try again.");
        return "redirect:/tasks/new";
    }
}
