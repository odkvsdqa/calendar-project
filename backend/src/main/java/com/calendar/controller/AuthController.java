package com.calendar.controller;

import com.calendar.dto.AuthResponse;
import com.calendar.dto.LoginRequest;
import com.calendar.dto.RegisterRequest;
import com.calendar.dto.UserDTO;
import com.calendar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用戶登入
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("登入請求: " + loginRequest.getUsername());
            
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("登入失敗: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * 用戶註冊
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("註冊請求: " + registerRequest.getUsername());
            
            UserDTO user = authService.register(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "註冊成功");
            response.put("user", user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.err.println("註冊失敗: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * 登出（前端處理，後端可選）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        System.out.println("用戶登出");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }
}