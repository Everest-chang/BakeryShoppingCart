package demo.usercart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.usercart.model.Order;
import demo.usercart.model.User;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	List<Order> findByUserUsername(String username);

}
