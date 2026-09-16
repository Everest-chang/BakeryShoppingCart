package demo.usercart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import demo.usercart.model.Order;
import demo.usercart.model.OrderItem;

@Mapper
public interface OrderMapper {
	
	int save(Order order);
	
	int saveItem(OrderItem item);
	
	List<Order> findByUserUsername(@Param("username") String username);
	
	List<Order> findAll();
	
	Order findById(@Param("id") Integer orderid);

}
