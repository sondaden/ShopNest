package com.sonnhuynhh.shopnest.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common model attributes to all views
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Add the current request path to all views for active menu highlighting
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
