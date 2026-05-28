package com.example.ecommerce.controller;

import com.example.ecommerce.common.Result;
import com.example.ecommerce.dto.UserQueryDTO;
import com.example.ecommerce.dto.UserStatusDTO;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AdminUserService;
import com.example.ecommerce.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<PageVO<User>> listUsers(@ModelAttribute UserQueryDTO query) {
        return Result.success(adminUserService.listUsers(query));
    }

    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO request) {
        adminUserService.updateStatus(id, request.getStatus());
        return Result.success("状态已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success("用户已删除", null);
    }
}
