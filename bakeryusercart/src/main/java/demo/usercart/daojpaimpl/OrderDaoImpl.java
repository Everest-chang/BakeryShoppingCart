package demo.usercart.daojpaimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.OrderDao;
import demo.usercart.model.Order;
import demo.usercart.repository.OrderRepository;

@Repository("OrderDaoJpa")
public class OrderDaoImpl implements OrderDao {
	
	@Autowired
	OrderRepository orderRepository;

	@Override
	public Order save(Order order) {
		return orderRepository.save(order);
	}

	@Override
	public List<Order> findByUserUsername(String username) {
		return orderRepository.findByUserUsername(username);
	}

	@Override
	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	@Override
	public Order findById(Integer orderid) {
		return orderRepository.findById(orderid).orElse(null);
	}

}
