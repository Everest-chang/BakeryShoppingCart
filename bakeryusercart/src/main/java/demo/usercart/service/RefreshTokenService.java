package demo.usercart.service;

import java.time.Instant;

public interface RefreshTokenService {

    // 登入成功後，建立 Refresh Token
    IssuedRefreshToken createToken(String username);

    // 使用 Refresh Token 換發新的長短 token
    RefreshResult refresh(String rawToken);

    // 建立 Refresh Token 的回傳資料
    record IssuedRefreshToken(
            String rawToken,
            Instant expiresAt
    ) {
    }

    // 換發結果
    record RefreshResult(
            RefreshStatus status,
            String accessToken,
            String username,
            String refreshToken,
            Instant refreshTokenExpiresAt
    ) {

        // 換發失敗時，不攜帶任何 token
        public static RefreshResult failure(
                RefreshStatus status
        ) {
            return new RefreshResult(
                    status,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    // 換發狀態
    enum RefreshStatus {
        SUCCESS,   // 換發成功
        INVALID,   // token 不存在或格式不合法
        EXPIRED,   // token 已到期
        REVOKED,   // token 已撤銷
        REUSED     // 已使用的舊 token 再次出現
    }
    // 登出：撤銷同一次登入產生的所有 Refresh Token
    void logout(String rawToken);
}