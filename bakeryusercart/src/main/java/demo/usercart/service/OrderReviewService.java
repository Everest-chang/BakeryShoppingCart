package demo.usercart.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import demo.usercart.dto.CreateOrderReviewRequest;
import demo.usercart.dto.OrderReviewResponse;
import demo.usercart.dto.UpdateOrderReviewRequest;

public interface OrderReviewService {

	// 新增評論
	OrderReviewResponse createReview(String username, CreateOrderReviewRequest request, List<MultipartFile> images);

	// 修改評論
	OrderReviewResponse updateReview(String username, Long reviewId, UpdateOrderReviewRequest request,
			List<MultipartFile> images);

	// 訂單明細頁：查詢自己的訂單評論
	// 訂單存在且屬於本人，但尚未評論時回傳 null
	OrderReviewResponse getReviewByOrderId(String username, Integer orderId);

	// 公開評論列表：keyword 空白時列出全部
	Page<OrderReviewResponse> getReviews(String keyword, int page, int size);

	// 取得指定評論的圖片內容
	ReviewImageContent getImage(Long reviewId, Long imageId);

	// 供 Controller 回傳圖片使用，不直接轉成 JSON
	record ReviewImageContent(String contentType, byte[] data) {
	}
}