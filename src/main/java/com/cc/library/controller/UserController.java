package com.cc.library.controller;

import com.cc.library.common.Result;
import com.cc.library.dto.UserRegistrationRequest;
import com.cc.library.entity.User;
import com.cc.library.exception.BusinessException;
import com.cc.library.service.UserService;
import com.cc.library.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Value("${app.security.admin-register-code}")
    private String adminRegisterCode;

    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public Result<User> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // 根据role字段和注册码设置角色
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            // 如果请求角色是ADMIN，必须提供正确的注册码
            if (request.getRegisterCode() == null || !request.getRegisterCode().equals(adminRegisterCode)) {
                throw new BusinessException("INVALID_REGISTER_CODE", "管理员注册码错误");
            }
            user.setRole(User.UserRole.ADMIN);
        } else {
            // 其他情况（包括role为空或READER），注册为普通用户
            user.setRole(User.UserRole.READER);
        }

        user.setStatus(User.UserStatus.ACTIVE);
        return Result.success(userService.createUser(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Assuming only admin can create users via this endpoint
    public Result<User> createUser(@Valid @RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return Result.success(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (#id == authentication.principal.id or hasRole('ADMIN'))")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated() and (#username == authentication.name or hasRole('ADMIN'))")
    public Result<User> getUserByUsername(@PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(userService.getAllUsers(pageable));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserDto> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            // Call the new service method that returns DTO
            return Result.success(userService.getUserProfileDto(currentUser.getId()));
        } else {
            return Result.error("无法获取当前用户信息");
        }
    }
} 