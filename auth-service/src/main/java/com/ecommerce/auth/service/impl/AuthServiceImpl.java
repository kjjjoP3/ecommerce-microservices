package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.AppUser;
import com.ecommerce.auth.repository.AppUserRepository;
import com.ecommerce.auth.security.JwtTokenProvider;
import com.ecommerce.auth.service.AuthService;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AtomicLong idGenerator = new AtomicLong(1000);

    public AuthServiceImpl(AppUserRepository appUserRepository, JwtTokenProvider jwtTokenProvider) {
        this.appUserRepository = appUserRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRoleName());
        return new LoginResponse(token, "Bearer");
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        appUserRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new IllegalArgumentException("Username already exists");
        });

        AppUser newUser = new AppUser(
                idGenerator.incrementAndGet(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                "USER"
        );
        appUserRepository.save(newUser);

        String token = jwtTokenProvider.generateToken(newUser.getUsername(), newUser.getRoleName());
        return new LoginResponse(token, "Bearer");
    }
}
