package com.example.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerce.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM sys_user WHERE deleted = 0 AND user_type = 2 AND (username = #{account} OR email = #{account} OR phone = #{account}) LIMIT 1")
    Admin selectAdminByAccount(@Param("account") String account);
}
