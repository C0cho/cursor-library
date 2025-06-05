package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.dto.LoginRequest;
import com.cc.library.dto.LoginResponse;
import com.cc.library.entity.User;
import com.cc.library.exception.BusinessException;
import com.cc.library.service.UserService;
import com.cc.library.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getUserByUsername(userDetails.getUsername());

            String token = jwtUtil.generateToken(userDetails);

            LoginResponse loginResponse = new LoginResponse(user, token);

            return Result.success(loginResponse);

        } catch (AuthenticationException e) {
            throw new BusinessException("INVALID_CREDENTIALS", "用户名或密码错误");
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
} 