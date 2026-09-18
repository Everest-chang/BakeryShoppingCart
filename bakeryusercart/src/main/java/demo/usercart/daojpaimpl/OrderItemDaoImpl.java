package demo.usercart.daojpaimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.dao.OrderItemDao;
import demo.usercart.model.OrderItem;
import demo.usercart.repository.ItemRepository;

@Repository("OrderItemDaoJpa")
public class OrderItemDaoImpl implements OrderItemDao{
	
	@Autowired
	ItemRepository itemRepository;

	@Override
	public List<OrderItem> findByOrderId(Integer orderid) {
		return itemRepository.findByOrderId(orderid);
	}

}
