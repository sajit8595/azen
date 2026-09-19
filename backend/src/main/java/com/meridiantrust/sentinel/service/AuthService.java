package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.AppUser;
import com.meridiantrust.sentinel.dto.Dtos.LoginRequest;
import com.meridiantrust.sentinel.dto.Dtos.LoginResponse;
import com.meridiantrust.sentinel.repository.AppUserRepository;
import com.meridiantrust.sentinel.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getUsername());
    }
}
