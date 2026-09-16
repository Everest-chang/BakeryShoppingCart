package demo.usercart.dto;

import java.time.Instant;
import java.util.List;

public record OrderReviewResponse(

		// 評論 ID
		Long id,

		// 匿名帳號，由後端轉換，例如 p***r
		String displayUsername,

		// 評分與評論文字
		Integer rating, String content,

		// 建立與修改時間
		Instant createdAt, Instant updatedAt,

		// 修改評論時檢查版本
		Long version,

		// 此訂單購買的商品
		List<ProductSummary> products,

		// 評論圖片資訊，不包含圖片二進位內容
		List<ImageSummary> images) {

	public record ProductSummary(Integer productId, String productTitle) {
	}

	public record ImageSummary(Long id, String url) {
	}
}
