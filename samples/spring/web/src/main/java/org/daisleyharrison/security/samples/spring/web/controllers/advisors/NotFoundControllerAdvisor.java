package org.daisleyharrison.security.samples.spring.web.controllers.advisors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class NotFoundControllerAdvisor {

     @ExceptionHandler(NoHandlerFoundException.class)
     public ModelAndView handle(Exception ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error.twig");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
        }
}