package demo.usercart.model;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;


@Component
public class JwtUtility {

    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000L;

    static {
        try {
            privateKey = loadPrivateKey();
            publicKey = loadPublicKey();

            System.out.println("RSA 公鑰、私鑰載入成功");

        } catch (Exception e) {
            throw new RuntimeException("RSA 金鑰載入失敗", e);
        }
    }


    // =========================
    // 讀取私鑰
    // =========================
    private static PrivateKey loadPrivateKey() throws Exception {

        InputStream inputStream =
                JwtUtility.class.getResourceAsStream(
                        "/keys/private.pem"
                );

        if (inputStream == null) {
            throw new RuntimeException("找不到 private.pem");
        }

        String key = new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );

        key = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes =
                Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }


    // =========================
    // 讀取公鑰
    // =========================
    private static PublicKey loadPublicKey() throws Exception {

        InputStream inputStream =
                JwtUtility.class.getResourceAsStream(
                        "/keys/public.pem"
                );

        if (inputStream == null) {
            throw new RuntimeException("找不到 public.pem");
        }

        String key = new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );

        key = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes =
                Base64.getDecoder().decode(key);

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }


 // 產生短效 Access Token
    public static String generateToken(String username) {

        Date now = new Date();

        Date expiresAt = new Date(
                now.getTime() + ACCESS_TOKEN_EXPIRATION_MS
        );

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }


    // =========================
    // 驗證 JWT
    // =========================
    public static boolean validateToken(String token) {

        try {

            String username =
                    Jwts.parser()
                            .verifyWith(publicKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject();

            return username != null;

        } catch (Exception e) {

            System.out.println(
                    "validateToken error: "
                            + e.getMessage()
            );

            return false;
        }
    }


    // =========================
    // 從 JWT 取得 username
    // =========================
    public String extractUsername(String token) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        return claims.getSubject();
    }
    // 從完整的 Authorization Header 驗證並取得會員帳號
    public String extractUsernameFromAuthorization(
            String authorization
    ) {

        // 1. 檢查 Header 是否存在
        if (authorization == null || authorization.isBlank()) {
            throw new JwtException("缺少登入憑證");
        }

        // 2. 拆開認證方式與 token
        String[] parts = authorization.trim().split("\\s+", 2);

        if (parts.length != 2
                || !"Bearer".equalsIgnoreCase(parts[0])) {
            throw new JwtException("登入憑證格式錯誤");
        }

        String token = parts[1].trim();

        if (token.isEmpty()) {
            throw new JwtException("登入憑證不能為空");
        }

        // 3. 沿用原本的方法，驗證簽章、到期時間並取得帳號
        String username = extractUsername(token);

        // 4. 確認 token 包含會員帳號
        if (username == null || username.isBlank()) {
            throw new JwtException("登入憑證缺少會員帳號");
        }

        return username;
    }
}