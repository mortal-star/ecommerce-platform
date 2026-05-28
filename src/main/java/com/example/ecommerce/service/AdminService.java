package com.example.ecommerce.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.entity.Admin;

public interface AdminService extends IService<Admin> {

    LoginResponse login(LoginRequest request);
}
