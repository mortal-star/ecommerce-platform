package com.example.ecommerce.service;

import com.example.ecommerce.dto.UserQueryDTO;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.vo.PageVO;

public interface AdminUserService {
    PageVO<User> listUsers(UserQueryDTO query);

    User getUserDetail(Long id);

    void updateStatus(Long id, Integer status);

    void deleteUser(Long id);
}
