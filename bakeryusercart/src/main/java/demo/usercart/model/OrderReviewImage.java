package demo.usercart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "order_review_images",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_image_sort",
                        columnNames = {
                                "review_id",
                                "sort_order"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OrderReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 圖片所屬的評論
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private OrderReview review;

    // 由後端檢查圖片內容後決定，例如 image/jpeg
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    // 圖片大小，單位為 bytes
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    // 顯示順序，由後端設定為 0～9
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    // 圖片的二進位內容
    @Lob
    @Column(name = "image_data", nullable = false,
            columnDefinition = "LONGBLOB")
    private byte[] imageData;
}