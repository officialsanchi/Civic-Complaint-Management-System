package com.civic.complaint.service;

import com.civic.complaint.dto.request.AuthRequest;
import com.civic.complaint.dto.response.AuthResponse;
import com.civic.complaint.exception.BadRequestException;
import com.civic.complaint.enums.Role;
import com.civic.complaint.model.User;
import com.civic.complaint.repository.UserRepository;
import com.civic.complaint.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .role(Role.CITIZEN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Password@1");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(2L).username(u.getUsername()).email(u.getEmail())
                    .password(u.getPassword()).fullName(u.getFullName())
                    .role(Role.CITIZEN).enabled(true).createdAt(LocalDateTime.now())
                    .build();
            return u;
        });
        when(jwtUtils.generateToken(any())).thenReturn("jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        AuthResponse.JwtToken result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.getUser().getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("Should throw when username already taken")
    void register_DuplicateUsername_Throws() {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setUsername("testuser");
        request.setEmail("other@example.com");
        request.setPassword("pass");
        request.setFullName("Test");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("Should throw when email already registered")
    void register_DuplicateEmail_Throws() {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setUsername("uniqueuser");
        request.setEmail("test@example.com");
        request.setPassword("pass");
        request.setFullName("Test");

        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Should login successfully")
    void login_Success() {
        AuthRequest.Login request = new AuthRequest.Login();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtUtils.generateToken(testUser)).thenReturn("jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        AuthResponse.JwtToken result = authService.login(request);

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Should change password successfully")
    void changePassword_Success() {
        AuthRequest.ChangePassword request = new AuthRequest.ChangePassword();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword@1");

        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword@1")).thenReturn("new_encoded");
        when(userRepository.save(testUser)).thenReturn(testUser);

        authService.changePassword(request, testUser);

        verify(userRepository).save(testUser);
        assertThat(testUser.getPassword()).isEqualTo("new_encoded");
    }

    @Test
    @DisplayName("Should throw when current password is wrong")
    void changePassword_WrongCurrentPassword_Throws() {
        AuthRequest.ChangePassword request = new AuthRequest.ChangePassword();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword@1");

        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(request, testUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("incorrect");
    }
}
