package demo.usercart.dao;

import java.util.List;

import demo.usercart.model.Order;

public interface OrderDao {
	
	Order save(Order order);
	
	List<Order> findByUserUsername(String username);
	
	List<Order> findAll();
	
	Order findById(Integer orderid);

}
