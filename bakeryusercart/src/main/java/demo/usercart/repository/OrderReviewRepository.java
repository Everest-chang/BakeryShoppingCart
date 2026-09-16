package demo.usercart.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import demo.usercart.model.OrderReview;

public interface OrderReviewRepository
        extends JpaRepository<OrderReview, Long> {

    // 檢查指定訂單是否已經有評論
    boolean existsByOrder_Id(Integer orderId);

    // 查詢指定訂單的評論
    Optional<OrderReview> findByOrder_Id(Integer orderId);

    // 公開評論列表：最新評論排前面，支援分頁
    Page<OrderReview> findAllByOrderByCreatedAtDescIdDesc(
            Pageable pageable
    );
    
 // 關鍵字搜尋：評論文字或訂單商品名稱符合即可
    @Query(
        value = """
            SELECT r
            FROM OrderReview r
            WHERE LOWER(r.content) LIKE LOWER(:pattern) ESCAPE '!'
               OR EXISTS (
                    SELECT item.id
                    FROM OrderItem item
                    WHERE item.order = r.order
                      AND LOWER(item.productTitle)
                          LIKE LOWER(:pattern) ESCAPE '!'
               )
            ORDER BY r.createdAt DESC, r.id DESC
            """,
        countQuery = """
            SELECT COUNT(r)
            FROM OrderReview r
            WHERE LOWER(r.content) LIKE LOWER(:pattern) ESCAPE '!'
               OR EXISTS (
                    SELECT item.id
                    FROM OrderItem item
                    WHERE item.order = r.order
                      AND LOWER(item.productTitle)
                          LIKE LOWER(:pattern) ESCAPE '!'
               )
            """
    )
    Page<OrderReview> searchByKeyword(
            @Param("pattern") String pattern,
            Pageable pageable
    );
}
