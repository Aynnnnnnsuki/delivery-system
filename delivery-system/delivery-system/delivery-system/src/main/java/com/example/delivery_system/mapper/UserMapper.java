package com.example.delivery_system.mapper;
import com.example.delivery_system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND password = #{password}")
    User login(String username, String password);
}