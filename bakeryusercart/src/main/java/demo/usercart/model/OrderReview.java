package demo.usercart.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "order_reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_review_order",
                        columnNames = "order_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OrderReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 一張訂單只能有一則評論
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 評分：Service 會驗證必須為 1～5
    @Column(nullable = false)
    private Integer rating;

    // 評論文字
    @Column(nullable = false, length = 2000)
    private String content;

    // 建立時間，由後端設定
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
 // 最後修改時間；尚未修改時為 null
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 樂觀鎖版本，由 JPA 自動管理
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
