package demo.usercart.model;

public enum DeliveryMethod {

	HOME_DELIVERY("黑貓宅配", 140), SEVEN_ELEVEN("7-11 取貨", 60), FAMILY_MART("全家取貨", 60), STORE_PICKUP("到店取貨", 0);

	private final String displayName;
	private final int shippingFee;

	DeliveryMethod(String displayName, int shippingFee) {
		this.displayName = displayName;
		this.shippingFee = shippingFee;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getShippingFee() {
		return shippingFee;
	}
}
