package com.secure.notes.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        // Check if CSRF token is available
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // CSRF protection is disabled, return a message
        if (token == null) {
            return ResponseEntity.ok(Map.of("message", "CSRF protection is disabled for JWT authentication."));
        }

        // If you really want to return CSRF token (though not needed for JWT)
        return ResponseEntity.ok(Map.of("csrf_token", token.getToken()));
    }
}
