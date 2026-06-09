package com.civic.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String userNameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
