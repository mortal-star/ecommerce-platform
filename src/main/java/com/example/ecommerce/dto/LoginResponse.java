package com.example.ecommerce.dto;

import java.util.List;

public class LoginResponse {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private Integer userType;

    private List<String> roles;

    public LoginResponse(String token, Long userId, String username, String nickname, Integer userType, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.userType = userType;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getUserType() {
        return userType;
    }

    public List<String> getRoles() {
        return roles;
    }
}
