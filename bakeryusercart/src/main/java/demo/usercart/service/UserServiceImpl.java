package demo.usercart.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import demo.usercart.dao.UserDao;
import demo.usercart.dto.LoginResult;
import demo.usercart.model.JwtUtility;
import demo.usercart.model.User;
import demo.usercart.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	@Qualifier("userDaoMybatis")
	UserDao userDao;
	@Autowired
	JwtUtility jwtUtility;
	@Autowired
	private RefreshTokenService refreshTokenService;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@Override
	public LoginResult login(User loginUser){
		 // 1. 檢查帳號與密碼是否有填寫
	    if (loginUser == null
	            || loginUser.getUsername() == null
	            || loginUser.getUsername().isBlank()
	            || loginUser.getPassword() == null
	            || loginUser.getPassword().isBlank()) {

	        return null;
	    }

	    // 2. 查詢會員
	    User user = userDao.findByUsername(
	            loginUser.getUsername()
	    );

	    // 3. 驗證密碼
	    if (user == null
	            || !passwordEncoder.matches(
	                    loginUser.getPassword(),
	                    user.getPassword()
	            )) {

	        return null;
	    }

	    // 4. 產生短效 Access Token
	    String accessToken = JwtUtility.generateToken(
	            user.getUsername()
	    );

	    // 5. 產生長效 Refresh Token，並將雜湊值存入資料庫
	    RefreshTokenService.IssuedRefreshToken issuedRefreshToken =
	            refreshTokenService.createToken(
	                    user.getUsername()
	            );

	    // 6. 將結果交給 Controller
	    return new LoginResult(
	            accessToken,
	            user.getUsername(),
	            issuedRefreshToken.rawToken(),
	            issuedRefreshToken.expiresAt()
	    );
	}

	@Override
	public String register(User user) {
		// 帳號不能空白
		if (user.getUsername() == null || user.getUsername().isBlank()) {
			return "帳號不能空白";
		}

		// 密碼不能空白
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			return "密碼不能空白";
		}

		// 帳號已存在
		if (userDao.existsByUsername(user.getUsername())) {
			return "此帳號已經存在";
		}

		// Email 已存在
		if (userDao.existsByEmail(user.getEmail())) {
			return "此信箱已經註冊";
		}
		
		  // ① 先 BCrypt 加密
	    String encodedPassword =
	            passwordEncoder.encode(user.getPassword());

	    // ② 把加密後的密碼放回 User
	    user.setPassword(encodedPassword);

	    // ③ 再存進資料庫
	    userDao.save(user);

	    return null;
		   
	}
}
