package demo.usercart.dao;

import demo.usercart.model.User;

public interface UserDao {
	
	User findByUsername(String username);
	
	boolean existsByUsername(String username);
	
	boolean existsByEmail(String email);
	
	User save(User user);

}
