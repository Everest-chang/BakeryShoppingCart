package demo.usercart.service;

import java.util.Map;

import demo.usercart.dto.LoginResult;
import demo.usercart.model.User;

public interface UserService {
	
	 // 登入：回傳 token 與會員資訊
    LoginResult login(User loginUser);

    String register(User user);

}
