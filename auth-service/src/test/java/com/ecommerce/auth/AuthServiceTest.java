package com.ecommerce.auth;

import com.ecommerce.auth.entity.RefreshTokenEntity;
import com.ecommerce.auth.entity.RoleEntity;
import com.ecommerce.auth.entity.UserEntity;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.RefreshTokenService;
import com.ecommerce.common.dto.auth.AuthResponse;
import com.ecommerce.common.dto.auth.LoginRequest;
import com.ecommerce.common.dto.auth.RegisterRequest;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.JwtUtils;
import com.ecommerce.common.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private RoleEntity userRole;

    @BeforeEach
    void setUp() {
        userRole = RoleEntity.builder().id(1L).name(Role.ROLE_USER).build();
    }

    @Test
    @DisplayName("Successfully register a new user")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .build();

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        UserEntity savedUser = UserEntity.builder()
                .id(10L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("hashed-password")
                .roles(Set.of(userRole))
                .build();

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtUtils.generateToken(eq(10L), eq("john.doe@example.com"), any())).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(10L)).thenReturn(
                RefreshTokenEntity.builder().token("mock-refresh-token").expiryDate(Instant.now().plusSeconds(3600)).build()
        );

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("john.doe@example.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Register throws DuplicateResourceException when email already exists")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .password("SecurePass123!")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login fails with BadCredentialsException mapped to UnauthorizedException")
    void testLoginBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Successful login returns tokens and user info")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("correctpassword")
                .build();

        UserPrincipal principal = UserPrincipal.create(5L, "test@example.com", List.of("ROLE_USER"));
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UserEntity user = UserEntity.builder()
                .id(5L)
                .email("test@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .roles(Set.of(userRole))
                .build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(eq(5L), eq("test@example.com"), any())).thenReturn("access-token-xyz");
        when(refreshTokenService.createRefreshToken(5L)).thenReturn(
                RefreshTokenEntity.builder().token("refresh-token-xyz").expiryDate(Instant.now().plusSeconds(3600)).build()
        );

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-xyz", response.getAccessToken());
        assertEquals("refresh-token-xyz", response.getRefreshToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }
}
