package demo.usercart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.usercart.model.OrderItem;

public interface ItemRepository extends JpaRepository<OrderItem, Integer> {
	public List<OrderItem> findByOrderId(Integer id);
	

}
