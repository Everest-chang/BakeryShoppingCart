package demo.usercart.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenUtility {

    private final SecureRandom secureRandom = new SecureRandom();

    // 產生 Refresh Token 原始值
    public String generateToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    // 計算 SHA-256 雜湊值
    public String hashToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh Token 不能為空"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "系統不支援 SHA-256",
                    e
            );
        }
    }
}
