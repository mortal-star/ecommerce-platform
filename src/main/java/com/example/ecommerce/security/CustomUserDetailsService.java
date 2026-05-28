package com.example.ecommerce.security;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByAccount(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        return new SecurityUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getStatus(), user.getUserType(), roles);
    }
}
