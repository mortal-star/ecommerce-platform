package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.dto.ResetPasswordRequest;
import com.example.ecommerce.dto.UpdateProfileRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.service.EmailCodeService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final EmailCodeService emailCodeService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailCodeService emailCodeService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailCodeService = emailCodeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        emailCodeService.verifyCode(request.getEmail(), "register", request.getCode());
        if (existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setUserType(1);
        user.setStatus(1);
        user.setGender(0);
        user.setDeleted(0);
        save(user);
        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByAccount(request.getAccount());
        if (user == null || !Integer.valueOf(1).equals(user.getUserType())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BadCredentialsException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("账号或密码错误");
        }
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        String token = jwtUtil.generateToken(user.getUsername(), Map.of("userId", user.getId(), "userType", user.getUserType(), "roles", roles));
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getUserType(), roles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        emailCodeService.verifyCode(request.getEmail(), "reset-password", request.getCode());
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .eq(User::getDeleted, 0), false);
        if (user == null) {
            throw new IllegalArgumentException("邮箱未注册");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        if (user == null || !Integer.valueOf(0).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail()) && existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(user.getPhone()) && existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("手机号已被使用");
        }
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        updateById(user);
        return user;
    }

    @Override
    public User getProfile(Long userId) {
        User user = getById(userId);
        if (user == null || !Integer.valueOf(0).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    private boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>().eq(User::getUsername, username).eq(User::getDeleted, 0)) > 0;
    }

    private boolean existsByEmail(String email) {
        return count(new LambdaQueryWrapper<User>().eq(User::getEmail, email).eq(User::getDeleted, 0)) > 0;
    }

    private boolean existsByPhone(String phone) {
        return count(new LambdaQueryWrapper<User>().eq(User::getPhone, phone).eq(User::getDeleted, 0)) > 0;
    }
}
