package demo.usercart.service;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import demo.usercart.model.OrderItem;

public interface OrderItemService {
	List<OrderItem> getItemsById(Integer orderid);

}
