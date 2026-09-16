package demo.usercart.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import demo.usercart.mapper.UserMapper;
import demo.usercart.model.User;

@Repository("userDaoMybatis")
public class UserDaoMybatisImpl implements UserDao {
	
	@Autowired
	UserMapper userMapper;

	@Override
	public User findByUsername(String username) {
		return userMapper.findByUsername(username);
	}

	@Override
	public boolean existsByUsername(String username) {
		return userMapper.existsByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userMapper.existsByEmail(email);
	}

	@Override
	public User save(User user) {
		userMapper.insert(user);
	    return user;
	}

}
