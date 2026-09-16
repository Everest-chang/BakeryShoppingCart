package demo.usercart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.usercart.model.OrderReviewImage;

public interface OrderReviewImageRepository
        extends JpaRepository<OrderReviewImage, Long> {

    // 依照顯示順序，查詢某則評論的全部圖片
    List<OrderReviewImage> findByReview_IdOrderBySortOrderAsc(
            Long reviewId
    );

    // 查詢指定評論底下的某張圖片
    Optional<OrderReviewImage> findByIdAndReview_Id(
            Long imageId,
            Long reviewId
    );

    // 計算某則評論的圖片數量
    long countByReview_Id(Long reviewId);
}