package demo.usercart.service;

import java.util.List;

import demo.usercart.dto.CheckoutRequest;
import demo.usercart.model.Order;

public interface OrderService {
	
	Order createOrder(Order order, String token);
	
	List<Order> getOrdersByUsername(String username);

    List<Order> getAllOrders();

    Order getOrdersById(Integer orderid);
    
 // 結帳：驗證商品、配送與付款資料，計算金額並建立訂單
    Order checkout(
            String username,
            CheckoutRequest request
    );

}
