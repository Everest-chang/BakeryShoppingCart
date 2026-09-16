package demo.usercart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import demo.usercart.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
