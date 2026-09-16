package demo.usercart.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 所屬會員，一位會員可以有多筆登入紀錄
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 只儲存 token 的雜湊值，不儲存原始 token
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    // 同一次登入所衍生的 token，使用相同的 familyId
    @Column(name = "family_id", nullable = false, length = 36)
    private String familyId;

    // 建立時間
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // 到期時間
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // 使用時間：已使用的 token 不可再次換發
    @Column(name = "used_at")
    private Instant usedAt;

    // 撤銷時間：登出或發現異常時設定
    @Column(name = "revoked_at")
    private Instant revokedAt;
}
