package demo.usercart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import demo.usercart.model.OrderItem;

@Mapper
public interface OrderItemMapper {
	
	List<OrderItem> findByOrderId(@Param("orderid") Integer orderid);

}
