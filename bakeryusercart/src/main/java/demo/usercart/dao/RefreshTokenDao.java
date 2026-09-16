package demo.usercart.dao;

import java.time.Instant;
import java.util.Optional;

import demo.usercart.model.RefreshToken;

public interface RefreshTokenDao {
	
	 // 儲存新增或修改後的 Refresh Token
    RefreshToken save(RefreshToken refreshToken);

    // 依照雜湊值查詢，並鎖定該筆資料
    Optional<RefreshToken> findByTokenHashForUpdate(
            String tokenHash
    );

    // 撤銷同一次登入產生的所有 token
    int revokeFamily(
            String familyId,
            Instant revokedAt
    );

}
