package demo.usercart.dto;

public record CreateOrderReviewRequest(

		// 要評論的訂單
		Integer orderId,

		// 必填，Service 驗證必須為 1～5
		Integer rating,

		// 評論文字，最多 2,000 字
		String content) {
}
