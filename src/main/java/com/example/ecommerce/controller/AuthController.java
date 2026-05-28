package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.EmailCodeRequest;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.dto.ResetPasswordRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AdminService;
import com.example.ecommerce.service.EmailCodeService;
import com.example.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    private final AdminService adminService;

    private final EmailCodeService emailCodeService;

    public AuthController(UserService userService, AdminService adminService, EmailCodeService emailCodeService) {
        this.userService = userService;
        this.adminService = adminService;
        this.emailCodeService = emailCodeService;
    }

    @PostMapping("/email-code")
    public Result<Map<String, String>> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        String scene = request.getScene() == null ? "register" : request.getScene();
        String code = emailCodeService.sendCode(request.getEmail(), scene);
        return Result.success("验证码已生成", Map.of("email", request.getEmail(), "scene", scene, "code", code));
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        user.setPassword(null);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    @PostMapping("/admin/login")
    public Result<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return Result.success("管理员登录成功", adminService.login(request));
    }

    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }
}
