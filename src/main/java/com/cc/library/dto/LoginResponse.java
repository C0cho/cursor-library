package com.cc.library.dto;

import com.cc.library.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String code;
    private String message;
    private LoginData data;

    public LoginResponse(User user, String token) {
        this.code = "200";
        this.message = "登录成功";
        this.data = new LoginData(user, token);
    }

    @Data
    public static class LoginData {
        private Long id;
        private String username;
        private User.UserRole role;
        private String token;

        public LoginData(User user, String token) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.role = user.getRole();
            this.token = token;
        }
    }
} 