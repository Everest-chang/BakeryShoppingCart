package demo.usercart.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import demo.usercart.dao.OrderDao;
import demo.usercart.dao.ProductDao;
import demo.usercart.dao.UserDao;
import demo.usercart.dto.CheckoutRequest;
import demo.usercart.exception.CheckoutException;
import demo.usercart.exception.CheckoutException.Reason;
import demo.usercart.model.JwtUtility;
import demo.usercart.model.Order;
import demo.usercart.model.OrderItem;
import demo.usercart.model.PaymentStatus;
import demo.usercart.model.Product;
import demo.usercart.model.User;
import demo.usercart.repository.OrderRepository;
import demo.usercart.repository.UserRepository;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	@Qualifier("OrderDaoMybatis")
	private OrderDao orderDao;
	@Autowired
	private JwtUtility jwtUtility;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	@Qualifier("ProductDaoMybatis")
	private ProductDao productDao;
	@Autowired
	@Qualifier("UserDaoMybatis")
	private UserDao userDao;

	@Override
	public Order createOrder(Order order, String token) {
		String username = jwtUtility.extractUsername(token.replace("Bearer ", ""));
		User user = userRepository.findByUsername(username);
		if (user == null) {
			return null;
		}
		order.setUser(user);
		order.setOrderTime(LocalDateTime.now());
		for (OrderItem item : order.getItems()) {
			item.setOrder(order);
		}
		return orderDao.save(order);
	}

	@Override
	public List<Order> getOrdersByUsername(String username) {
		System.out.println("find user " + username);
		List<Order> data = orderDao.findByUserUsername(username);
		System.out.println("found user order is " + data);
		return data;
	}

	@Override
	public List<Order> getAllOrders() {
		List<Order> orders = orderDao.findAll();
		orders.replaceAll(o -> {
			o.setItems(null);
			return o;
		});
		return orders;
	}

	@Override
	public Order getOrdersById(Integer orderid) {
		Order order = orderDao.findById(orderid);
		if (order != null) {
			order.setItems(null);
		}
		return order;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Order checkout(String username, CheckoutRequest request) {

		// 1. 確認會員
		if (username == null || username.isBlank()) {
			throw new CheckoutException(Reason.UNAUTHORIZED, "請重新登入");
		}

		User user = userDao.findByUsername(username);

		if (user == null) {
			throw new CheckoutException(Reason.UNAUTHORIZED, "會員不存在，請重新登入");
		}

		// 2. 檢查購物車
		if (request == null || request.items() == null || request.items().isEmpty()) {

			throw new CheckoutException(Reason.INVALID_INPUT, "購物車不能為空");
		}

		// 3. 驗證配送與付款方式
		if (request.deliveryMethod() == null) {
			throw new CheckoutException(Reason.INVALID_INPUT, "請選擇配送方式");
		}

		if (request.paymentMethod() == null || !request.paymentMethod().supports(request.deliveryMethod())) {

			throw new CheckoutException(Reason.INVALID_INPUT, "請選擇此配送方式支援的付款方式");
		}

		// 4. 驗證收件人
		String recipientName = requireCheckoutText(request.recipientName(), "收件人姓名", 100);

		String recipientPhone = requireCheckoutText(request.recipientPhone(), "收件人手機", 20);

		// 目前採台灣手機格式：09 開頭，共 10 碼
		if (!recipientPhone.matches("09\\d{8}")) {
			throw new CheckoutException(Reason.INVALID_INPUT, "手機號碼需為 09 開頭的 10 碼數字");
		}

		Order order = new Order();

		order.setUser(user);
		order.setRecipientName(recipientName);
		order.setRecipientPhone(recipientPhone);
		order.setDeliveryMethod(request.deliveryMethod());
		order.setPaymentMethod(request.paymentMethod());
		order.setPaymentStatus(PaymentStatus.PENDING);

		ZoneId taipei = ZoneId.of("Asia/Taipei");
		order.setOrderTime(LocalDateTime.now(taipei));

		// 5. 只保存所選配送方式需要的資料
		switch (request.deliveryMethod()) {

		case HOME_DELIVERY -> {
			order.setDeliveryAddress(requireCheckoutText(request.deliveryAddress(), "配送地址", 500));
		}

		case SEVEN_ELEVEN, FAMILY_MART -> {
			order.setPickupStoreName(requireCheckoutText(request.pickupStoreName(), "超商門市名稱", 100));

			order.setPickupStoreCode(requireCheckoutText(request.pickupStoreCode(), "超商門市代碼", 20));
		}

		case STORE_PICKUP -> {
			LocalDate pickupDate = request.pickupDate();

			if (pickupDate == null) {
				throw new CheckoutException(Reason.INVALID_INPUT, "請選擇取貨日期");
			}

			if (pickupDate.isBefore(LocalDate.now(taipei))) {
				throw new CheckoutException(Reason.INVALID_INPUT, "取貨日期不能早於今天");
			}

			order.setPickupDate(pickupDate);
		}
		}

		// 6. 查詢資料庫價格，建立訂單商品明細
		Set<Integer> productIds = new HashSet<>();
		var orderItems = new ArrayList<OrderItem>();

		BigDecimal subtotal = BigDecimal.ZERO;

		for (CheckoutRequest.CheckoutItem item : request.items()) {

			if (item == null || item.productId() == null || item.productId() <= 0 || item.quantity() == null
					|| item.quantity() <= 0) {

				throw new CheckoutException(Reason.INVALID_INPUT, "商品編號或購買數量不正確");
			}

			// 正常購物車已合併相同商品，不接受重複商品列
			if (!productIds.add(item.productId())) {
				throw new CheckoutException(Reason.INVALID_INPUT, "購物車包含重複商品，請重新整理購物車");
			}

			Product product = productDao.findById(item.productId());

			if (product == null) {
				throw new CheckoutException(Reason.PRODUCT_NOT_FOUND, "商品不存在，編號：" + item.productId());
			}

			double databasePrice = product.getPrice();

			if (!Double.isFinite(databasePrice) || databasePrice < 0) {

				throw new IllegalStateException("商品價格資料不正確，編號：" + product.getId());
			}

			BigDecimal unitPrice = BigDecimal.valueOf(databasePrice);

			// 目前訂單金額使用 int，商品價格需為整數元
			try {
				unitPrice.intValueExact();
			} catch (ArithmeticException e) {
				throw new IllegalStateException("商品價格必須為可儲存的整數元，編號：" + product.getId(), e);
			}

			subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.quantity())));

			OrderItem orderItem = new OrderItem();

			orderItem.setOrder(order);
			orderItem.setPid(product.getId());
			orderItem.setProductTitle(product.getTitle());
			orderItem.setProductPrice(unitPrice.intValueExact());
			orderItem.setQuantity(item.quantity());

			orderItems.add(orderItem);
		}

		// 7. 後端決定運費與總金額
		int shippingFee = request.deliveryMethod().getShippingFee();

		try {
			order.setSubtotal(subtotal.intValueExact());

			order.setTotalPrice(subtotal.add(BigDecimal.valueOf(shippingFee)).intValueExact());
		} catch (ArithmeticException e) {
			throw new CheckoutException(Reason.INVALID_INPUT, "訂單金額超過系統可處理範圍");
		}

		order.setShippingFee(shippingFee);
		order.setItems(orderItems);

		// 8. 儲存訂單及全部商品明細
		return orderDao.save(order);
	}

	private String requireCheckoutText(String value, String fieldName, int maxLength) {

		if (value == null || value.isBlank()) {
			throw new CheckoutException(Reason.INVALID_INPUT, "請填寫" + fieldName);
		}

		String result = value.strip();

		if (result.codePointCount(0, result.length()) > maxLength) {
			throw new CheckoutException(Reason.INVALID_INPUT, fieldName + "最多 " + maxLength + " 字");
		}

		return result;
	}

}
