package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.LoginRequest;
import com.meridiantrust.sentinel.dto.Dtos.LoginResponse;
import com.meridiantrust.sentinel.service.AuthService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
