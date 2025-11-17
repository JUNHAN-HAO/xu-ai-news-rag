package com.xu.news.controller;

import com.xu.news.config.JwtService;
import com.xu.news.dto.AuthRequest;
import com.xu.news.dto.AuthResponse;
import com.xu.news.dto.RegisterRequest;
import com.xu.news.entity.User;
import com.xu.news.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            User.Role.USER
        );
        
        String token = jwtService.generateToken(user);
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        AuthResponse response = AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(response);
    }
}

