package com.example.delivery_system.controller;

import com.example.delivery_system.entity.User;
import com.example.delivery_system.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    // 1. 访问首页跳转登录
    @GetMapping("/")
    public String index() {
        return "login";
    }

    // 2. 处理登录逻辑
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        // 查询数据库
        User user = userMapper.login(username, password);

        // 分支一：登录成功 (User 不为空)
        if (user != null) {
            session.setAttribute("user", user); // 存入Session

            // 根据角色跳转不同页面
            if (user.getRole() == 1) {
                return "redirect:/merchant/list"; // 商家
            } else if (user.getRole() == 2) {
                return "redirect:/student/list";  // 学生
            } else if (user.getRole() == 0) {
                return "redirect:/admin/home";    // 管理员
            } else {
                // 防御性代码：防止数据库里有未知的角色ID
                return "redirect:/";
            }
        }
        // 分支二：登录失败 (User 为空)
        else {
            model.addAttribute("msg", "账号或密码错误");
            return "login"; // 返回登录页并显示错误
        }
    }

    // 3. 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 清空 Session
        return "redirect:/";
    }
}