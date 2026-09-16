package demo.usercart.dao;

import java.util.List;

import demo.usercart.model.OrderItem;

public interface OrderItemDao {
	
	List<OrderItem> findByOrderId(Integer orderid);

}
