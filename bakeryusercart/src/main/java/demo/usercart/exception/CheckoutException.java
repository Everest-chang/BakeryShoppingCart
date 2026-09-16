package demo.usercart.exception;

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
}
