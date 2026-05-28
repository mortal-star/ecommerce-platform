package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.UpdateProfileRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.security.SecurityUserDetails;
import com.example.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<User> getProfile(@AuthenticationPrincipal SecurityUserDetails principal) {
        return Result.success(userService.getProfile(principal.getUserId()));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<User> updateProfile(@AuthenticationPrincipal SecurityUserDetails principal, @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(principal.getUserId(), request);
        user.setPassword(null);
        return Result.success("个人信息修改成功", user);
    }
}
