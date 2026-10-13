package com.ems.controller;

import com.ems.config.AuthStore;
import com.ems.model.Admin;
import com.ems.repository.AdminRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AdminRepository admins;
    private final AuthStore store;
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    public AuthController(AdminRepository admins, AuthStore store) { this.admins = admins; this.store = store; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Admin a = admins.findByUsername(body.getOrDefault("username", "")).orElse(null);
        if (a == null || !enc.matches(body.getOrDefault("password", ""), a.getPassword()))
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        return ResponseEntity.ok(Map.of("token", store.issue(), "username", a.getUsername()));
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String auth) {
        store.revoke(auth.replace("Bearer ", ""));
        return Map.of("message", "Logged out");
    }
}
