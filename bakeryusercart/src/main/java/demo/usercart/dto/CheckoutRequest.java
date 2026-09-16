package demo.usercart.dto;

import java.time.LocalDate;
import java.util.List;

import demo.usercart.model.DeliveryMethod;
import demo.usercart.model.PaymentMethod;

public record CheckoutRequest(

		// 購買商品與數量
		List<CheckoutItem> items,

		// 配送方式
		DeliveryMethod deliveryMethod,

		// 收件人
		String recipientName, String recipientPhone,

		// 黑貓宅配使用
		String deliveryAddress,

		// 超商取貨使用
		String pickupStoreName, String pickupStoreCode,

		// 到店取貨使用
		LocalDate pickupDate,

		// 付款方式
		PaymentMethod paymentMethod) {

	public record CheckoutItem(

			// 商品編號
			Integer productId,

			// 購買數量
			Integer quantity) {
	}
}
