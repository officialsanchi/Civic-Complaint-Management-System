package com.civic.complaint.service.impl;

import com.civic.complaint.config.JwtService;
import com.civic.complaint.dto.request.*;
import com.civic.complaint.dto.response.AuthResponse;
import com.civic.complaint.exception.BadRequestException;
import com.civic.complaint.enums.Role;
import com.civic.complaint.exception.ResourceNotFoundException;
import com.civic.complaint.model.BlacklistedToken;
import com.civic.complaint.model.PasswordResetToken;
import com.civic.complaint.model.User;
import com.civic.complaint.repository.BlacklistedTokenRepository;
import com.civic.complaint.repository.PasswordResetTokenRepository;
import com.civic.complaint.repository.UserRepository;
import com.civic.complaint.security.UserPrincipal;
import com.civic.complaint.utile.EmailService;
import com.civic.complaint.utile.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final BlacklistedTokenRepository tokenRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exist");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exist");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .otherName(request.getOtherName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CITIZEN)
                .build();

        User saved = userRepository.save(user);

        AuthResponse response = buildAuthResponse(saved);
        response.setMessage("Account created successfully");

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserNameOrEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository
                .findByUsernameOrEmail(request.getUserNameOrEmail(), request.getUserNameOrEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {

        UserPrincipal principal = new UserPrincipal(user);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(jwtService.generateRefreshToken(principal))
                .user(userMapper.toResponse(user))
                .build();
    }
    public String logout(String token) {

        BlacklistedToken blacklistedToken =
                BlacklistedToken.builder()
                        .token(token)
                        .blacklistedAt(LocalDateTime.now())
                        .build();

        tokenRepository.save(blacklistedToken);

        return "Logout successful";
    }
    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Passwords do not match");
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);
    }
    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(
                        request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusMinutes(30))
                        .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                token);
    }
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request) {

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid token"));

        if (token.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Token expired");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Passwords do not match");
        }

        User user = token.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        userRepository.save(user);

        passwordResetTokenRepository.delete(token);
    }
}
