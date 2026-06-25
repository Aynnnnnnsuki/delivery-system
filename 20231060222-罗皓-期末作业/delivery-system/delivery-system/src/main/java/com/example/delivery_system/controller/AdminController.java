package com.example.delivery_system.controller;

import com.example.delivery_system.entity.User;
import com.example.delivery_system.mapper.AdminMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminMapper adminMapper;

    // 管理员首页 (数据看板 + 用户列表)
    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        // 1. 权限校验
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 0) { // 0是管理员
            return "redirect:/";
        }

        // 2. 加载数据统计
        model.addAttribute("totalUsers", adminMapper.countUsers());
        model.addAttribute("totalOrders", adminMapper.countOrders());
        model.addAttribute("totalMerchants", adminMapper.countMerchants());

        // 3. 加载用户列表
        List<User> userList = adminMapper.findAllUsers();
        model.addAttribute("users", userList);

        return "admin_home";
    }

    // 删除用户
    @GetMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam Long userId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getRole() == 0) {
            adminMapper.deleteUser(userId);
        }
        return "redirect:/admin/home";
    }
}