package demo.usercart.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import demo.usercart.model.OrderReview;

public interface OrderReviewDao {

    // 新增或修改評論
    OrderReview save(OrderReview review);

    // 依評論 ID 查詢，供修改評論使用
    Optional<OrderReview> findById(Long reviewId);

    // 檢查指定訂單是否已有評論
    boolean existsByOrderId(Integer orderId);

    // 查詢指定訂單的評論
    Optional<OrderReview> findByOrderId(Integer orderId);

    // 分頁列出全部評論，最新的排前面
    Page<OrderReview> findAll(Pageable pageable);

    // 依評論內容或訂單商品名稱搜尋
    // pattern 是 Service 處理後的 LIKE 搜尋字串
    Page<OrderReview> searchByKeyword(
            String pattern,
            Pageable pageable
    );
}