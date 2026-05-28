package com.example.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerce.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE deleted = 0 AND (username = #{account} OR email = #{account} OR phone = #{account}) LIMIT 1")
    User selectByAccount(@Param("account") String account);

    @Select("SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON ur.role_id = r.id AND ur.deleted = 0 WHERE r.deleted = 0 AND r.status = 1 AND ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
