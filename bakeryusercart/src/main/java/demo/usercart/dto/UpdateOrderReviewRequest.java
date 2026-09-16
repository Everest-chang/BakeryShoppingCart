package demo.usercart.dto;

import java.util.List;

public record UpdateOrderReviewRequest(

		// 使用者載入評論時取得的版本
		Long version,

		// 修改後的評分，必須為 1～5
		Integer rating,

		// 修改後的評論文字，最多 2,000 字
		String content,

		// 要保留的既有圖片 ID
		// 空清單表示刪除全部既有圖片
		List<Long> retainedImageIds) {
}
