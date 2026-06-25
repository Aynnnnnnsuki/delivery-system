package com.example.delivery_system.controller;

import com.example.delivery_system.entity.Dish;
import com.example.delivery_system.entity.User;
import com.example.delivery_system.mapper.DishMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DishController {

    @Autowired
    private DishMapper dishMapper;

    // --- 商家功能 ---
    @GetMapping("/merchant/list")
    public String merchantList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) return "redirect:/"; // 权限拦截

        List<Dish> dishes = dishMapper.findByMerchantId(user.getUserId());
        model.addAttribute("dishes", dishes);
        return "merchant_list";
    }

    @PostMapping("/merchant/add")
    public String addDish(Dish dish, HttpSession session) {
        User user = (User) session.getAttribute("user");
        dish.setMerchantId(user.getUserId());
        dishMapper.insert(dish);
        return "redirect:/merchant/list";
    }

    // --- 学生功能 ---
    @GetMapping("/student/list")
    public String studentList(HttpSession session, Model model) {
        // 展示所有菜品
        List<Dish> dishes = dishMapper.findAll();
        model.addAttribute("dishes", dishes);
        return "student_list";
    }

    // --- 修改：学生购买接口 (变成 Post 请求，因为要传地址) ---
    // 修改原来的 buy 方法
    @PostMapping("/student/buy")
    public String buy(@RequestParam Long dishId,
                      @RequestParam String address,
                      HttpSession session,
                      Model model) { // 加个 Model 用于传回错误信息

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/";

        // 1. 准备参数 Map
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getUserId());
        params.put("dishId", dishId);
        params.put("address", address);
        params.put("result", null); // 这个key用来接收存储过程的输出

        try {
            // 2. 调用存储过程 (一行代码搞定所有业务！)
            dishMapper.callPlaceOrder(params);

            // 3. 获取数据库返回的结果
            String dbResult = (String) params.get("result");

            System.out.println("数据库返回: " + dbResult); // 在控制台打印看看

            if (dbResult.contains("成功")) {
                return "redirect:/student/list?success=true";
            } else {
                // 如果返回"库存不足"或"菜品不存在"
                return "redirect:/student/list?error=" + URLEncoder.encode(dbResult, "UTF-8");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/student/list?error=sys_error";
        }
    }

    // --- 新增：商家查看所有订单 ---
    @GetMapping("/merchant/orders")
    public String merchantOrders(Model model) {
        List<Map<String, Object>> orders = dishMapper.findAllOrders();
        model.addAttribute("orders", orders);
        return "merchant_orders"; // 跳转到新页面
    }

    // --- 新增：商家发货/送达 ---
    @GetMapping("/merchant/updateStatus")
    public String updateStatus(@RequestParam Long orderId, @RequestParam int status) {
        dishMapper.updateStatus(orderId, status);
        return "redirect:/merchant/orders";
    }
}
