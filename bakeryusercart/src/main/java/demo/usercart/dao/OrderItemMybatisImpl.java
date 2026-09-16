package demo.usercart.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.mapper.OrderItemMapper;
import demo.usercart.model.OrderItem;

@Repository("orderItemDaoMybatis")
public class OrderItemMybatisImpl implements OrderItemDao{
	
	@Autowired
	OrderItemMapper orderItemMapper;

	@Override
	public List<OrderItem> findByOrderId(Integer orderid) {
		return orderItemMapper.findByOrderId(orderid);
	}

}
