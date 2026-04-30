package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
