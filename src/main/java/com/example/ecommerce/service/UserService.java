package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.dto.ChangePasswordDTO;
import com.example.ecommerce.dto.ResetPasswordRequest;
import com.example.ecommerce.dto.UpdateProfileRequest;
import com.example.ecommerce.entity.User;

public interface UserService extends IService<User> {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void resetPassword(ResetPasswordRequest request);

    User updateProfile(Long userId, UpdateProfileRequest request);

    User getProfile(Long userId);

    void changePassword(Long userId, ChangePasswordDTO request);
}
