package demo.usercart.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import demo.usercart.model.User;

@Mapper
public interface UserMapper {
	
	User findByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);
    
    int insert(User user);


}
