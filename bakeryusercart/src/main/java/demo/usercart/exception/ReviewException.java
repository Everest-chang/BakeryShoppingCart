package demo.usercart.exception;

public class ReviewException extends RuntimeException {

	private final Reason reason;

	public ReviewException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}

	public enum Reason {

		// 評分、文字、圖片等輸入不符合規則
		INVALID_INPUT,

		// 訂單、評論或圖片不存在
		NOT_FOUND,

		// 無權操作其他會員的訂單或評論
		FORBIDDEN,

		// 已評論或修改版本衝突
		CONFLICT
	}
}