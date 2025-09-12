package com.divacode.ecom_proj.controller;

import com.divacode.ecom_proj.dto.AuthRequest;
import com.divacode.ecom_proj.security.CustomUserDetailsService;
import com.divacode.ecom_proj.security.JwtUtil;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(), authRequest.getPassword()));
        System.out.println("logggggggggggggggg");
        System.out.println(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extract UserDetails or username
        String username = authentication.getName();  // simplest way

        String token = jwtUtil.generateToken(username); // adapt to your JwtUtil method signature

        return ResponseEntity.ok(Map.of("token", token));
    }
}


