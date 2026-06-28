package com.aicc.backend.service;

import com.aicc.backend.config.jwt.JwtTokenProvider;
import com.aicc.backend.domain.Role;
import com.aicc.backend.domain.User;
import com.aicc.backend.dto.auth.AuthResponse;
import com.aicc.backend.dto.auth.LoginRequest;
import com.aicc.backend.dto.auth.SignupRequest;
import com.aicc.backend.exception.ApiException;
import com.aicc.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.badRequest("이미 가입된 이메일입니다.");
        }
        User user = userRepository.save(User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.CUSTOMER)
                .build());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw ApiException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = tokenProvider.createToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}
