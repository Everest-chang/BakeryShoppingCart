package demo.usercart.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.usercart.dto.LoginResult;
import demo.usercart.model.User;
import demo.usercart.service.RefreshTokenService;
import demo.usercart.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    // 正式環境預設只允許透過 HTTPS 傳送 Cookie
    @Value("${app.auth.cookie-secure:true}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {

        // 1. 驗證帳密並建立長短 token
        LoginResult result = userService.login(loginUser);

        if (result == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "帳號或密碼錯誤"
                    ));
        }

        // 2. 計算 Refresh Token 剩餘有效時間
        Duration remainingLifetime = Duration.between(
                Instant.now(),
                result.refreshTokenExpiresAt()
        );

        // 3. 建立 Refresh Token Cookie
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/user")
                .maxAge(remainingLifetime)
                .build();

        // 4. Cookie 傳送 Refresh Token；JSON 傳送 Access Token
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        String error = userService.register(user);

        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok("會員建立成功");
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String rawToken
    ) {

        // 1. 驗證舊 Refresh Token，並換發新的長短 token
        RefreshTokenService.RefreshResult result =
                refreshTokenService.refresh(rawToken);

        // 2. 驗證失敗，清除瀏覽器的 Refresh Token Cookie
        if (result.status()
                != RefreshTokenService.RefreshStatus.SUCCESS) {

            ResponseCookie clearCookie = ResponseCookie
                    .from("refreshToken", "")
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .sameSite("Strict")
                    .path("/api/user")
                    .maxAge(Duration.ZERO)
                    .build();

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(
                            HttpHeaders.SET_COOKIE,
                            clearCookie.toString()
                    )
                    .body(Map.of(
                            "message", "登入已失效，請重新登入"
                    ));
        }

        // 3. 計算新 Refresh Token 的剩餘效期
        Duration remainingLifetime = Duration.between(
                Instant.now(),
                result.refreshTokenExpiresAt()
        );

        // 4. 使用新 Refresh Token 覆蓋原本的 Cookie
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/user")
                .maxAge(remainingLifetime)
                .build();

        // 5. JSON 只回傳 Access Token 與會員帳號
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(Map.of(
                        "accessToken", result.accessToken(),
                        "username", result.username()
                ));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refreshToken",
                    required = false
            ) String rawToken
    ) {

        // 1. 撤銷資料庫中同一次登入的 Refresh Token
        refreshTokenService.logout(rawToken);

        // 2. 清除瀏覽器的 Refresh Token Cookie
        ResponseCookie clearCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/user")
                .maxAge(Duration.ZERO)
                .build();

        // 3. 回傳 204，表示成功且沒有回應內容
        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearCookie.toString()
                )
                .build();
    }
}