package com.ianctchinese.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GreetingController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/greeting")
    public ResponseEntity<Map<String, String>> getGreeting() {
        Map<String, String> response = new HashMap<>();
        
        response.put("welcome", messageSource.getMessage("greeting.welcome", null, LocaleContextHolder.getLocale()));
        response.put("platform", messageSource.getMessage("greeting.platform", null, LocaleContextHolder.getLocale()));
        response.put("description", messageSource.getMessage("greeting.description", null, LocaleContextHolder.getLocale()));
        response.put("locale", LocaleContextHolder.getLocale().toString());
        
        return ResponseEntity.ok(response);
    }
}