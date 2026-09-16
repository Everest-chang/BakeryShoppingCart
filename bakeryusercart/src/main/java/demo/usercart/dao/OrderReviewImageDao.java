package demo.usercart.dao;

import java.util.List;
import java.util.Optional;

import demo.usercart.model.OrderReviewImage;

public interface OrderReviewImageDao {

    // 儲存新增或修改後的圖片
    OrderReviewImage save(OrderReviewImage image);

    // 查詢某則評論的圖片，依顯示順序排列
    List<OrderReviewImage> findByReviewId(Long reviewId);

    // 查詢指定評論底下的某張圖片
    Optional<OrderReviewImage> findByIdAndReviewId(
            Long imageId,
            Long reviewId
    );

    // 計算某則評論的圖片數量
    long countByReviewId(Long reviewId);

    // 刪除指定圖片
    void delete(OrderReviewImage image);

    // 將待處理的資料變更送到資料庫，不提交交易
    void flush();
}