package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.entity.Admin;
import com.example.ecommerce.mapper.AdminMapper;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.AdminService;
import com.example.ecommerce.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final AdminMapper adminMapper;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AdminServiceImpl(AdminMapper adminMapper, UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminMapper.selectAdminByAccount(request.getAccount());
        if (admin == null) {
            throw new BadCredentialsException("管理员账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BadCredentialsException("管理员账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("管理员账号或密码错误");
        }
        List<String> roles = userMapper.selectRoleCodesByUserId(admin.getId());
        String token = jwtUtil.generateToken(admin.getUsername(), Map.of("userId", admin.getId(), "userType", admin.getUserType(), "roles", roles));
        admin.setLastLoginTime(LocalDateTime.now());
        updateById(admin);
        return new LoginResponse(token, admin.getId(), admin.getUsername(), admin.getNickname(), admin.getUserType(), roles);
    }
}
