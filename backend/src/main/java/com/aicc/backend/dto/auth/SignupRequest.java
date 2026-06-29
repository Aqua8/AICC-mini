package com.aicc.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
                message = "영문, 숫자, 특수문자(@$!%*#?&)를 포함한 8자 이상이어야 합니다."
        ) String password,
        @NotBlank String name
) {
}
