package demo.usercart.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<?> handleProductNotFound(ProductNotFoundException e) {

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "message", e.getMessage()));
	}

	// Access Token 已過期
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<?> handleExpiredJwt(ExpiredJwtException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("status", 401, "code", "ACCESS_TOKEN_EXPIRED", "message", "登入憑證已過期"));
	}

	// JWT 格式錯誤、簽章驗證失敗等問題
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<?> handleInvalidJwt(JwtException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("status", 401, "code", "ACCESS_TOKEN_INVALID", "message", "登入憑證無效"));
	}

	// 評論的業務驗證錯誤
	@ExceptionHandler(ReviewException.class)
	public ResponseEntity<?> handleReviewException(ReviewException e) {

		HttpStatus status = switch (e.getReason()) {
		case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
		case NOT_FOUND -> HttpStatus.NOT_FOUND;
		case FORBIDDEN -> HttpStatus.FORBIDDEN;
		case CONFLICT -> HttpStatus.CONFLICT;
		};

		return ResponseEntity.status(status)
				.body(Map.of("status", status.value(), "code", e.getReason().name(), "message", e.getMessage()));
	}

	// 上傳超過 multipart 設定的大小限制
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> handleUploadTooLarge(MaxUploadSizeExceededException e) {

		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(
				Map.of("status", 413, "code", "UPLOAD_TOO_LARGE", "message", "上傳大小超過限制：單一檔案最多 5 MB，" + "整個請求最多 30 MB"));
	}

	// JPA 樂觀鎖衝突
	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ResponseEntity<?> handleOptimisticLock(OptimisticLockingFailureException e) {

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("status", 409, "code", "VERSION_CONFLICT", "message", "資料已被其他操作修改，請重新載入後再試"));
	}

	// 資料庫唯一限制、外鍵或其他完整性限制衝突
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("status", 409, "code", "DATA_CONFLICT", "message", "資料儲存發生衝突，請重新載入確認後再試"));
	}

}
