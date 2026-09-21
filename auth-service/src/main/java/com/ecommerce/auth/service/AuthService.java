package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.RefreshTokenEntity;
import com.ecommerce.auth.entity.RoleEntity;
import com.ecommerce.auth.entity.UserEntity;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.common.dto.auth.*;
import com.ecommerce.common.enums.Role;
import com.ecommerce.common.exception.DuplicateResourceException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.exception.UnauthorizedException;
import com.ecommerce.common.security.JwtUtils;
import com.ecommerce.common.security.SecurityConstants;
import com.ecommerce.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + normalizedEmail + " already exists");
        }

        RoleEntity defaultRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(Role.ROLE_USER).build()));

        UserEntity user = UserEntity.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .enabled(true)
                .roles(Set.of(defaultRole))
                .build();

        UserEntity savedUser = userRepository.save(user);

        List<String> roleNames = List.of(defaultRole.getName().name());
        String accessToken = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail(), roleNames);
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
                .user(mapToUserDto(savedUser))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = principal.authorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList());

            String accessToken = jwtUtils.generateToken(principal.id(), principal.email(), roles);
            RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(principal.id());

            UserEntity user = userRepository.findById(principal.id())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.id()));

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
                    .user(mapToUserDto(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.verifyExpiration(request.getRefreshToken());
        UserEntity user = refreshTokenEntity.getUser();

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        String newAccessToken = jwtUtils.generateToken(user.getId(), user.getEmail(), roles);
        RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS / 1000)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(UserEntity user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .build();
    }
}
