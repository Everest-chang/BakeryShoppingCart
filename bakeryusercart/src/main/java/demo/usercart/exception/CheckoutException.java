package demo.usercart.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class CheckoutException extends RuntimeException {

	private final Reason reason;

	public CheckoutException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}

	public enum Reason {

		// 購物車、數量或收件資料不正確
		INVALID_INPUT,

		// 登入對應的會員不存在
		UNAUTHORIZED,

		// 商品不存在
		PRODUCT_NOT_FOUND
	}

	// 結帳驗證錯誤
	@ExceptionHandler(CheckoutException.class)
	public ResponseEntity<?> handleCheckoutException(CheckoutException e) {

		HttpStatus status = switch (e.getReason()) {
		case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
		case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
		case PRODUCT_NOT_FOUND -> HttpStatus.NOT_FOUND;
		};

		return ResponseEntity.status(status)
				.body(Map.of("status", status.value(), "code", e.getReason().name(), "message", e.getMessage()));
	}
}
