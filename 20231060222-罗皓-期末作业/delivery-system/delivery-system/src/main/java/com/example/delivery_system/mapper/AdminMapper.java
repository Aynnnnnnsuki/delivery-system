package com.example.delivery_system.mapper;

import com.example.delivery_system.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    // --- 数据统计看板 ---

    // 1. 统计总用户数
    @Select("SELECT COUNT(*) FROM sys_user")
    int countUsers();

    // 2. 统计总订单数
    @Select("SELECT COUNT(*) FROM trade_order")
    int countOrders();

    // 3. 统计商家数量
    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 1")
    int countMerchants();

    // --- 用户管理 ---

    // 4. 查询所有用户列表 (排除管理员自己)
    @Select("SELECT * FROM sys_user WHERE role != 0")
    List<User> findAllUsers();

    // 5. 删除用户 (封号)
    @Delete("DELETE FROM sys_user WHERE user_id = #{userId}")
    void deleteUser(Long userId);
}