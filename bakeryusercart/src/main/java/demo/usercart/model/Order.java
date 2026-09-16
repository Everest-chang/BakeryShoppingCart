package demo.usercart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "orders")
@ToString(exclude = { "items" })
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private LocalDateTime orderTime;

	private int totalPrice;
	
	// 商品小計，不含運費
	@Column(name = "subtotal")
	private Integer subtotal;

	// 下單當時的運費
	@Column(name = "shipping_fee")
	private Integer shippingFee;

	// 配送方式
	@Enumerated(EnumType.STRING)
	@Column(name = "delivery_method", length = 30)
	private DeliveryMethod deliveryMethod;

	// 收件人姓名
	@Column(name = "recipient_name", length = 100)
	private String recipientName;

	// 收件人手機
	@Column(name = "recipient_phone", length = 20)
	private String recipientPhone;

	// 黑貓宅配：配送地址
	@Column(name = "delivery_address", length = 500)
	private String deliveryAddress;

	// 超商取貨：門市名稱
	@Column(name = "pickup_store_name", length = 100)
	private String pickupStoreName;

	// 超商取貨：門市代碼
	@Column(name = "pickup_store_code", length = 20)
	private String pickupStoreCode;

	// 到店取貨：取貨日期
	@Column(name = "pickup_date")
	private LocalDate pickupDate;

	// 付款方式
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", length = 30)
	private PaymentMethod paymentMethod;

	// 付款狀態
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", length = 20)
	private PaymentStatus paymentStatus;

	@OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, targetEntity = OrderItem.class)
	private List<OrderItem> items = new ArrayList<>();
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnoreProperties("orders")
	private User user;

}
