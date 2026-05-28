package com.example.ecommerce.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerce.dto.UserQueryDTO;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AdminUserService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.vo.PageVO;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserService userService;

    public AdminUserServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public PageVO<User> listUsers(UserQueryDTO query) {
        long current = query.getPageNum() == null || query.getPageNum() < 1 ? 1L : query.getPageNum();
        long size = query.getPageSize() == null || query.getPageSize() < 1 ? 10L : Math.min(query.getPageSize(), 100L);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .eq(query.getUserType() != null, User::getUserType, query.getUserType())
                .orderByDesc(User::getCreateTime);

        String keyword = query.getKeyword();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getPhone, kw)
                    .or().like(User::getNickname, kw)
                    .or().like(User::getEmail, kw));
        }

        Page<User> page = userService.page(new Page<>(current, size), wrapper);
        // strip passwords
        page.getRecords().forEach(u -> u.setPassword(null));
        return new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Override
    public User getUserDetail(Long id) {
        User user = userService.getById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值非法");
        }
        User user = userService.getById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (Integer.valueOf(2).equals(user.getUserType())) {
            throw new IllegalArgumentException("不能修改管理员状态");
        }
        user.setStatus(status);
        userService.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userService.getById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (Integer.valueOf(2).equals(user.getUserType())) {
            throw new IllegalArgumentException("不能删除管理员");
        }
        user.setDeleted(1);
        userService.updateById(user);
    }
}
