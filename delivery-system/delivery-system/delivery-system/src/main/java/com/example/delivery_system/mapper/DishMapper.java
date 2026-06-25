package com.example.delivery_system.mapper;
import com.example.delivery_system.entity.Dish;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;
import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {
    // 商家查询自己的菜品
    @Select("SELECT * FROM dish WHERE merchant_id = #{merchantId}")
    List<Dish> findByMerchantId(Long merchantId);

    // 学生查询所有菜品
    @Select("SELECT * FROM dish")
    List<Dish> findAll();

    // 商家添加菜品
    @Insert("INSERT INTO dish(name, price, stock, merchant_id) VALUES(#{name}, #{price}, #{stock}, #{merchantId})")
    void insert(Dish dish);

    // 【重点修改】使用存储过程下单
    // 2. #{result, mode=OUT...} 是告诉 MyBatis，这个参数是数据库要还给我们的结果
    @Select("CALL proc_place_order(" +
            "#{userId, mode=IN}, " +
            "#{dishId, mode=IN}, " +
            "#{address, mode=IN}, " +
            "#{result, mode=OUT, jdbcType=VARCHAR}" +
            ")")
    @Options(statementType = StatementType.CALLABLE) // 必须加这句，声明是调用存储过程
    void callPlaceOrder(Map<String, Object> params);
    // 新增：商家发货/完成订单
    @Update("UPDATE trade_order SET status = #{status} WHERE order_id = #{orderId}")
    void updateStatus(Long orderId, int status);

    // 新增：查询商家收到的订单 (用于商家后台)
    @Select("SELECT * FROM trade_order ORDER BY create_time DESC")
// 注意：这里为了偷懒没做连表查询，直接查所有订单用于演示
    List<Map<String, Object>> findAllOrders();

    @Select("SELECT * FROM dish WHERE dish_id = #{dishId}")
    Dish findById(Long dishId);
}