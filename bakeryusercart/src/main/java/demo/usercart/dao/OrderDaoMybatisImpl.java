package demo.usercart.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.mapper.OrderMapper;
import demo.usercart.model.Order;
import demo.usercart.model.OrderItem;

@Repository("orderDaoMybatis")
public class OrderDaoMybatisImpl implements OrderDao{
	
	@Autowired
	OrderMapper orderMapper;

	@Override
	public Order save(Order order) {
		// 先新增 orders
        orderMapper.save(order);

        // 這時候 MySQL 自動產生的 id
        // 已經被塞回 order.id

        for (OrderItem item : order.getItems()) {

            item.setOrder(order);

            orderMapper.saveItem(item);
        }

        return order;
	}

	@Override
	public List<Order> findByUserUsername(String username) {
		return orderMapper.findByUserUsername(username);
	}

	@Override
	public List<Order> findAll() {
		return orderMapper.findAll();
	}

	@Override
	public Order findById(Integer orderid) {
		return orderMapper.findById(orderid);
	}
	
	
	
	

}
