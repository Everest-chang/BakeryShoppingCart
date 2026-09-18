package demo.usercart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import demo.usercart.dao.OrderItemDao;
import demo.usercart.model.OrderItem;
import demo.usercart.repository.ItemRepository;

@Service
public class OrderItemServiceImpl implements OrderItemService {
	
	@Autowired
	@Qualifier("OrderItemDaoMybatis")
	OrderItemDao orderItemDao;

	@Override
	public List<OrderItem> getItemsById(Integer orderid) {
		List<OrderItem> items=orderItemDao.findByOrderId(orderid);
		for(OrderItem i : items) {
			i.setOrder(null);
		}
		return items; 
	}

}
