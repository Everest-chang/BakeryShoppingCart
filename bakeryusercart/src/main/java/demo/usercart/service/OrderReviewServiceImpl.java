package demo.usercart.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import demo.usercart.dao.OrderDao;
import demo.usercart.dao.OrderItemDao;
import demo.usercart.dao.OrderReviewDao;
import demo.usercart.dao.OrderReviewImageDao;
import demo.usercart.dto.CreateOrderReviewRequest;
import demo.usercart.dto.OrderReviewResponse;
import demo.usercart.dto.UpdateOrderReviewRequest;
import demo.usercart.exception.ReviewException;
import demo.usercart.exception.ReviewException.Reason;
import demo.usercart.model.Order;
import demo.usercart.model.OrderReview;
import demo.usercart.model.OrderReviewImage;

@Service
public class OrderReviewServiceImpl implements OrderReviewService {

	private static final int MAX_IMAGES = 5;

	@Autowired
	@Qualifier("orderReviewDaoJpa")
	private OrderReviewDao reviewDao;

	@Autowired
	@Qualifier("orderReviewImageDaoJpa")
	private OrderReviewImageDao imageDao;

	@Autowired
	@Qualifier("orderDaoMybatis")
	private OrderDao orderDao;

	@Autowired
	@Qualifier("orderItemDaoMybatis")
	private OrderItemDao orderItemDao;

	// 下一步建立，負責驗證圖片內容及大小
	@Autowired
	private ReviewImageValidator imageValidator;

	// =========================
	// 新增評論
	// =========================
	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderReviewResponse createReview(String username, CreateOrderReviewRequest request,
			List<MultipartFile> images) {
		if (request == null || request.orderId() == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "請指定要評論的訂單");
		}

		Order order = requireOwnedOrder(username, request.orderId());

		validateRating(request.rating());
		String content = normalizeContent(request.content());

		if (reviewDao.existsByOrderId(order.getId())) {
			throw new ReviewException(Reason.CONFLICT, "此訂單已經評論，請使用修改功能");
		}

		// 圖片全部驗證完成後，才開始寫入
		var validatedImages = imageValidator.validate(images, MAX_IMAGES);

		OrderReview review = new OrderReview();
		review.setOrder(order);
		review.setRating(request.rating());
		review.setContent(content);
		review.setCreatedAt(Instant.now());

		review = reviewDao.save(review);

		int sortOrder = 0;

		for (var image : validatedImages) {
			OrderReviewImage entity = new OrderReviewImage();

			entity.setReview(review);
			entity.setContentType(image.contentType());
			entity.setSizeBytes((long) image.data().length);
			entity.setSortOrder(sortOrder++);
			entity.setImageData(image.data());

			imageDao.save(entity);
		}

		// 提前執行 SQL，檢查資料庫限制；尚未 commit
		imageDao.flush();

		return toResponse(review);
	}

	// =========================
	// 修改評論
	// =========================
	@Override
	@Transactional(rollbackFor = Exception.class)
	public OrderReviewResponse updateReview(String username, Long reviewId, UpdateOrderReviewRequest request,
			List<MultipartFile> images) {
		if (reviewId == null || request == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "缺少評論修改資料");
		}

		OrderReview review = reviewDao.findById(reviewId)
				.orElseThrow(() -> new ReviewException(Reason.NOT_FOUND, "找不到評論"));

		requireOwnedOrder(username, review.getOrder().getId());

		if (request.version() == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "缺少評論版本，請重新載入");
		}

		if (!Objects.equals(request.version(), review.getVersion())) {
			throw new ReviewException(Reason.CONFLICT, "評論已被修改，請重新載入後再編輯");
		}

		validateRating(request.rating());
		String content = normalizeContent(request.content());

		List<Long> retainedIds = request.retainedImageIds();

		if (retainedIds == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "請提供要保留的圖片清單");
		}

		if (retainedIds.size() > MAX_IMAGES || retainedIds.stream().anyMatch(Objects::isNull)
				|| new HashSet<>(retainedIds).size() != retainedIds.size()) {

			throw new ReviewException(Reason.INVALID_INPUT, "保留圖片清單不正確");
		}

		List<OrderReviewImage> existingImages = imageDao.findByReviewId(reviewId);

		Map<Long, OrderReviewImage> imageMap = existingImages.stream()
				.collect(Collectors.toMap(OrderReviewImage::getId, Function.identity()));

		for (Long imageId : retainedIds) {
			if (!imageMap.containsKey(imageId)) {
				throw new ReviewException(Reason.INVALID_INPUT, "保留的圖片不屬於此評論");
			}
		}

		var validatedImages = imageValidator.validate(images, MAX_IMAGES - retainedIds.size());

		// 驗證完成，開始修改
		review.setRating(request.rating());
		review.setContent(content);
		review.setUpdatedAt(Instant.now());

		reviewDao.save(review);

		// 讓 JPA 執行版本檢查，並更新評論
		imageDao.flush();

		var retainedSet = new HashSet<>(retainedIds);

		for (OrderReviewImage image : existingImages) {
			if (!retainedSet.contains(image.getId())) {
				imageDao.delete(image);
			}
		}

		imageDao.flush();

		// 暫時移到負數排序，避免重新排列時唯一值衝突
		// 交易成功前不會把這些中間值提交
		for (int i = 0; i < retainedIds.size(); i++) {
			OrderReviewImage image = imageMap.get(retainedIds.get(i));

			image.setSortOrder(-i - 1);
			imageDao.save(image);
		}

		imageDao.flush();

		// 將保留的圖片排回 0、1、2……
		for (int i = 0; i < retainedIds.size(); i++) {
			OrderReviewImage image = imageMap.get(retainedIds.get(i));

			image.setSortOrder(i);
			imageDao.save(image);
		}

		imageDao.flush();

		// 新圖片接在保留圖片之後
		int sortOrder = retainedIds.size();

		for (var image : validatedImages) {
			OrderReviewImage entity = new OrderReviewImage();

			entity.setReview(review);
			entity.setContentType(image.contentType());
			entity.setSizeBytes((long) image.data().length);
			entity.setSortOrder(sortOrder++);
			entity.setImageData(image.data());

			imageDao.save(entity);
		}

		imageDao.flush();

		return toResponse(review);
	}

	// =========================
	// 查詢自己的訂單評論
	// =========================
	@Override
	@Transactional(readOnly = true)
	public OrderReviewResponse getReviewByOrderId(String username, Integer orderId) {
		requireOwnedOrder(username, orderId);

		return reviewDao.findByOrderId(orderId).map(this::toResponse).orElse(null);
	}

	// =========================
	// 公開評論與關鍵字搜尋
	// =========================
	@Override
	@Transactional(readOnly = true)
	public Page<OrderReviewResponse> getReviews(String keyword, int page, int size) {
		if (page < 0 || size < 1 || size > 50) {
			throw new ReviewException(Reason.INVALID_INPUT, "頁碼不可小於 0，每頁筆數必須為 1～50");
		}

		var pageable = PageRequest.of(page, size);

		if (keyword == null || keyword.isBlank()) {
			return reviewDao.findAll(pageable).map(this::toResponse);
		}

		String text = keyword.strip();

		if (text.length() > 100) {
			throw new ReviewException(Reason.INVALID_INPUT, "搜尋關鍵字最多 100 字");
		}

		// 配合 Repository 的 ESCAPE '!'
		String escaped = text.replace("!", "!!").replace("%", "!%").replace("_", "!_");

		String pattern = "%" + escaped + "%";

		return reviewDao.searchByKeyword(pattern, pageable).map(this::toResponse);
	}

	// =========================
	// 取得評論圖片
	// =========================
	@Override
	@Transactional(readOnly = true)
	public ReviewImageContent getImage(Long reviewId, Long imageId) {
		if (reviewId == null || imageId == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "缺少圖片查詢資料");
		}

		OrderReviewImage image = imageDao.findByIdAndReviewId(imageId, reviewId)
				.orElseThrow(() -> new ReviewException(Reason.NOT_FOUND, "找不到圖片"));

		return new ReviewImageContent(image.getContentType(), image.getImageData());
	}

	// =========================
	// 共用驗證
	// =========================
	private Order requireOwnedOrder(String username, Integer orderId) {
		if (username == null || username.isBlank()) {
			throw new ReviewException(Reason.FORBIDDEN, "無權操作此訂單");
		}

		if (orderId == null) {
			throw new ReviewException(Reason.INVALID_INPUT, "缺少訂單編號");
		}

		Order order = orderDao.findById(orderId);

		if (order == null) {
			throw new ReviewException(Reason.NOT_FOUND, "找不到訂單");
		}

		if (order.getUser() == null || !username.equals(order.getUser().getUsername())) {

			throw new ReviewException(Reason.FORBIDDEN, "無權操作此訂單");
		}

		return order;
	}

	private void validateRating(Integer rating) {
		if (rating == null || rating < 1 || rating > 5) {
			throw new ReviewException(Reason.INVALID_INPUT, "請選擇 1～5 顆星的評分");
		}
	}

	private String normalizeContent(String content) {
		String result = content == null ? "" : content.strip();

		if (result.codePointCount(0, result.length()) > 2000) {
			throw new ReviewException(Reason.INVALID_INPUT, "評論文字最多 2,000 字");
		}

		return result;
	}

	// =========================
	// Entity 轉成公開 DTO
	// =========================
	private OrderReviewResponse toResponse(OrderReview review) {

		var products = orderItemDao.findByOrderId(review.getOrder().getId()).stream()
				.map(item -> new OrderReviewResponse.ProductSummary(item.getPid(), item.getProductTitle())).distinct()
				.toList();

		var images = imageDao.findByReviewId(review.getId()).stream()
				.map(image -> new OrderReviewResponse.ImageSummary(image.getId(),
						"/api/reviews/" + review.getId() + "/images/" + image.getId()))
				.toList();

		return new OrderReviewResponse(review.getId(), maskUsername(review.getOrder().getUser().getUsername()),
				review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt(),
				review.getVersion(), products, images);
	}

	private String maskUsername(String username) {
		if (username == null || username.isEmpty()) {
			return "***";
		}

		int[] characters = username.codePoints().toArray();

		if (characters.length == 1) {
			return "*";
		}

		String first = new String(Character.toChars(characters[0]));

		if (characters.length == 2) {
			return first + "*";
		}

		String last = new String(Character.toChars(characters[characters.length - 1]));

		return first + "***" + last;
	}
}