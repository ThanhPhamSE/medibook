package com.medibook.modules.auth.service;

import com.medibook.modules.auth.dto.request.LoginRequest;
import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
