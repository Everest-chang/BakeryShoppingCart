package demo.usercart.model;

public enum PaymentMethod {

	BANK_TRANSFER("銀行轉帳"), CASH_ON_DELIVERY("貨到付款"), CASH_AT_STORE("到店現金付款");

	private final String displayName;

	PaymentMethod(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	// 判斷這個付款方式是否適用於指定配送方式
	public boolean supports(DeliveryMethod deliveryMethod) {

		if (deliveryMethod == null) {
			return false;
		}

		return switch (deliveryMethod) {

		// 黑貓宅配只允許轉帳
		case HOME_DELIVERY -> this == BANK_TRANSFER;

		// 超商允許轉帳或貨到付款
		case SEVEN_ELEVEN, FAMILY_MART -> this == BANK_TRANSFER || this == CASH_ON_DELIVERY;

		// 到店取貨允許轉帳或現金
		case STORE_PICKUP -> this == BANK_TRANSFER || this == CASH_AT_STORE;
		};
	}
}