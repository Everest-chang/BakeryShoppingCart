package demo.usercart.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import demo.usercart.dto.CreateOrderReviewRequest;
import demo.usercart.dto.OrderReviewResponse;
import demo.usercart.dto.UpdateOrderReviewRequest;
import demo.usercart.model.JwtUtility;
import demo.usercart.service.OrderReviewService;

@RestController
@RequestMapping("/api/reviews")
public class OrderReviewController {

	@Autowired
	private OrderReviewService reviewService;

	@Autowired
	private JwtUtility jwtUtility;

	// =========================
	// 新增評論，需要登入
	// =========================
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<OrderReviewResponse> createReview(
			@RequestHeader(value = "Authorization", required = false) String authorization,

			@RequestPart("review") CreateOrderReviewRequest request,

			@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		String username = jwtUtility.extractUsernameFromAuthorization(authorization);

		OrderReviewResponse result = reviewService.createReview(username, request, images);

		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	// =========================
	// 修改評論，需要登入
	// =========================
	@PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<OrderReviewResponse> updateReview(
			@RequestHeader(value = "Authorization", required = false) String authorization,

			@PathVariable Long reviewId,

			@RequestPart("review") UpdateOrderReviewRequest request,

			@RequestPart(value = "images", required = false) List<MultipartFile> images) {
		String username = jwtUtility.extractUsernameFromAuthorization(authorization);

		OrderReviewResponse result = reviewService.updateReview(username, reviewId, request, images);

		return ResponseEntity.ok(result);
	}

	// =========================
	// 查詢自己的訂單評論
	// =========================
	@GetMapping("/order/{orderId}")
	public ResponseEntity<OrderReviewResponse> getReviewByOrderId(
			@RequestHeader(value = "Authorization", required = false) String authorization,

			@PathVariable Integer orderId) {
		String username = jwtUtility.extractUsernameFromAuthorization(authorization);

		OrderReviewResponse result = reviewService.getReviewByOrderId(username, orderId);

		// 訂單屬於本人，但尚未評論
		if (result == null) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(result);
	}

	// =========================
	// 公開評論列表與關鍵字搜尋
	// =========================
	@GetMapping
	public ResponseEntity<ReviewPageResponse> getReviews(@RequestParam(defaultValue = "") String keyword,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size) {
		var result = reviewService.getReviews(keyword, page, size);

		// 自訂分頁 JSON，避免直接依賴 Page 的序列化格式
		ReviewPageResponse response = new ReviewPageResponse(result.getContent(), result.getNumber(), result.getSize(),
				result.getTotalElements(), result.getTotalPages());

		return ResponseEntity.ok(response);
	}

	// =========================
	// 公開評論圖片
	// =========================
	@GetMapping("/{reviewId}/images/{imageId}")
	public ResponseEntity<byte[]> getImage(@PathVariable Long reviewId, @PathVariable Long imageId) {
		OrderReviewService.ReviewImageContent image = reviewService.getImage(reviewId, imageId);

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType()))
				.contentLength(image.data().length).header("X-Content-Type-Options", "nosniff")
				.cacheControl(CacheControl.noStore()).body(image.data());
	}

	// 公開列表回傳格式
	public record ReviewPageResponse(List<OrderReviewResponse> content, int page, int size, long totalElements,
			int totalPages) {
	}
}
