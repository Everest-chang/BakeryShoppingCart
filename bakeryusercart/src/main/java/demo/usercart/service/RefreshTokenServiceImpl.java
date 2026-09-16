package demo.usercart.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.usercart.dao.RefreshTokenDao;
import demo.usercart.dao.UserDao;
import demo.usercart.model.JwtUtility;
import demo.usercart.model.RefreshToken;
import demo.usercart.model.RefreshTokenUtility;
import demo.usercart.model.User;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    // Refresh Token 有效時間：7 天
    private static final Duration REFRESH_TOKEN_LIFETIME =
            Duration.ofDays(7);

    @Autowired
    @Qualifier("refreshTokenDaoJpa")
    private RefreshTokenDao refreshTokenDao;

    // 沿用目前會員功能使用的 MyBatis DAO
    @Autowired
    @Qualifier("userDaoMybatis")
    private UserDao userDao;

    @Autowired
    private RefreshTokenUtility refreshTokenUtility;

    // 必須在帳密驗證成功後呼叫
    @Override
    @Transactional
    public IssuedRefreshToken createToken(String username) {

        // 1. 查詢會員
        User user = userDao.findByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("會員不存在");
        }

        // 2. 計算建立時間與到期時間
        Instant now = Instant.now();
        Instant expiresAt = now.plus(REFRESH_TOKEN_LIFETIME);

        // 3. 產生原始 token 與雜湊值
        String rawToken = refreshTokenUtility.generateToken();
        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        // 4. 建立要儲存的資料
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(expiresAt);

        // 5. 透過 DAO 存入資料庫
        refreshTokenDao.save(refreshToken);

        // 6. 回傳原始 token，後續由 Controller 設定 Cookie
        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Override
    @Transactional
    public RefreshResult refresh(String rawToken) {

        // 1. 檢查 token 格式
        // 32 bytes 經過 Base64 URL 編碼並移除 padding 後為 43 字元
        if (rawToken == null
                || !rawToken.matches("[A-Za-z0-9_-]{43}")) {

            return RefreshResult.failure(
                    RefreshStatus.INVALID
            );
        }

        // 2. 計算雜湊值，查詢並鎖定舊 token
        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        RefreshToken oldToken = refreshTokenDao
                .findByTokenHashForUpdate(tokenHash)
                .orElse(null);

        if (oldToken == null) {
            return RefreshResult.failure(
                    RefreshStatus.INVALID
            );
        }

        Instant now = Instant.now();

        // 3. 已使用的 token 再次出現，撤銷整組登入紀錄
        if (oldToken.getUsedAt() != null) {

            refreshTokenDao.revokeFamily(
                    oldToken.getFamilyId(),
                    now
            );

            return RefreshResult.failure(
                    RefreshStatus.REUSED
            );
        }

        // 4. 檢查是否已被撤銷
        if (oldToken.getRevokedAt() != null) {
            return RefreshResult.failure(
                    RefreshStatus.REVOKED
            );
        }

        // 5. 檢查是否到期，等於到期時間也視為過期
        if (!now.isBefore(oldToken.getExpiresAt())) {
            return RefreshResult.failure(
                    RefreshStatus.EXPIRED
            );
        }

        // 6. 產生新的長短 token
        String username = oldToken.getUser().getUsername();

        String accessToken = JwtUtility.generateToken(username);

        String newRawToken = refreshTokenUtility.generateToken();

        String newTokenHash =
                refreshTokenUtility.hashToken(newRawToken);

        // 7. 標記舊 token 已使用
        oldToken.setUsedAt(now);
        refreshTokenDao.save(oldToken);

        // 8. 建立新 Refresh Token 紀錄
        RefreshToken newToken = new RefreshToken();

        newToken.setUser(oldToken.getUser());
        newToken.setTokenHash(newTokenHash);

        // 沿用同一次登入的 familyId
        newToken.setFamilyId(oldToken.getFamilyId());

        newToken.setCreatedAt(now);

        // 沿用原本到期時間，避免每次更新都延長 7 天
        newToken.setExpiresAt(oldToken.getExpiresAt());

        refreshTokenDao.save(newToken);

        // 9. 回傳新的長短 token
        return new RefreshResult(
                RefreshStatus.SUCCESS,
                accessToken,
                username,
                newRawToken,
                newToken.getExpiresAt()
        );
    }
    
    @Override
    @Transactional
    public void logout(String rawToken) {

        // 1. 沒有 token 或格式不合法，直接結束
        if (rawToken == null
                || !rawToken.matches("[A-Za-z0-9_-]{43}")) {
            return;
        }

        // 2. 計算雜湊值，查詢並鎖定 token
        String tokenHash = refreshTokenUtility.hashToken(rawToken);

        RefreshToken token = refreshTokenDao
                .findByTokenHashForUpdate(tokenHash)
                .orElse(null);

        // 3. 找不到 token，直接結束
        if (token == null) {
            return;
        }

        // 4. 撤銷這次登入產生的所有 Refresh Token
        refreshTokenDao.revokeFamily(
                token.getFamilyId(),
                Instant.now()
        );
    }
}