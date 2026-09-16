import { useState, useEffect } from 'react'
import {
  fetchOrders,
  fetchOrderById,
  fetchOrderItems
} from '../api/ApiService'
import OrderReviewForm from './OrderReviewForm'

const DELIVERY_LABELS = {
  HOME_DELIVERY: '黑貓宅配',
  SEVEN_ELEVEN: '7-11 取貨',
  FAMILY_MART: '全家取貨',
  STORE_PICKUP: '到店取貨'
}

const PAYMENT_LABELS = {
  BANK_TRANSFER: '銀行轉帳',
  CASH_ON_DELIVERY: '貨到付款',
  CASH_AT_STORE: '到店現金付款'
}

const PAYMENT_STATUS_LABELS = {
  PENDING: '待付款',
  PAID: '已付款'
}

function displayMoney(value) {
  return value == null
    ? '未提供'
    : `NT$ ${Number(value).toLocaleString('zh-TW')}`
}


function Orders({ isLoggedIn, username }) {

  const [orders, setOrders] = useState([])
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [orderItems, setOrderItems] = useState([])


  // =========================
  // 登入後抓取使用者訂單
  // =========================
  useEffect(() => {

    if (isLoggedIn && username) {

      fetchOrders(username)
        .then(setOrders)
        .catch(() => {
          setOrders([])
        })

    }

  }, [isLoggedIn, username])


  // =========================
  // 顯示某筆訂單明細
  // =========================
  async function handleShowDetails(orderId) {

    try {

      // 同時向後端抓：
      // 1. 訂單資料
      // 2. 訂單商品明細
      const [order, items] = await Promise.all([
        fetchOrderById(orderId),
        fetchOrderItems(orderId)
      ])

      setSelectedOrder(order)
      setOrderItems(items)

    } catch (error) {

      console.error('載入訂單明細失敗', error)

    }
  }


  // =========================
  // 尚未登入
  // =========================
  if (!isLoggedIn) {

    return (
      <main className="orders-page">

        <section className="orders-heading">

          <p className="orders-subtitle">
            MY ORDERS
          </p>

          <h1>訂單查詢</h1>

          <div className="title-line"></div>

        </section>

        <div className="orders-login-message">
          請先登入帳戶後查看訂單。
        </div>

      </main>
    )
  }


  return (

    <main className="orders-page">


      {/* =========================
          頁面標題
      ========================= */}

      <section className="orders-heading">

        <p className="orders-subtitle">
          MY ORDERS
        </p>

        <h1>訂單管理</h1>

        <div className="title-line"></div>

        <p className="orders-user">
          {username} 的訂單
        </p>

      </section>


      {/* =========================
          訂單列表
      ========================= */}

      <section className="orders-container">


        {orders.length === 0 ? (

          <div className="empty-orders">
            目前沒有訂單紀錄
          </div>

        ) : (

          <div className="orders-table">


            {/* 表頭 */}
            <div className="orders-row orders-header">

              <div>訂單編號</div>

              <div>訂購用戶</div>

              <div>訂單時間</div>

              <div>操作</div>

            </div>


            {/* 訂單資料 */}
            {orders.map(order => (

              <div
                className="orders-row"
                key={order.id}
              >

                <div className="order-number">
                  #{order.id}
                </div>


                <div>
                  {order.user?.username ?? username}
                </div>


                <div>
                  {order.orderTime
                    ? new Date(
                        order.orderTime
                      ).toLocaleString()
                    : '-'}
                </div>


                <div>

                  <button
                    className="order-detail-button"
                    onClick={() =>
                      handleShowDetails(order.id)
                    }
                  >
                    查看商品
                  </button>

                </div>

              </div>

            ))}

          </div>

        )}


        {/* =========================
            訂單商品明細
        ========================= */}

        {selectedOrder && (

          <section className="order-detail-section">


            <div className="order-detail-title">

              <div>

                <p className="detail-small-title">
                  ORDER DETAIL
                </p>

                <h2>
                  訂單 #{selectedOrder.id}
                </h2>

              </div>


              <button
                className="close-detail-button"
                onClick={() => {

                  setSelectedOrder(null)
                  setOrderItems([])

                }}
              >
                關閉
              </button>

            </div>


            {/* 訂單基本資訊 */}
            <div className="order-summary">

              <div>

                <span>訂單編號</span>

                <strong>
                  #{selectedOrder.id}
                </strong>

              </div>


              <div>

                <span>訂購帳戶</span>

                <strong>
                  {selectedOrder.user?.username ?? username}
                </strong>

              </div>


              <div>

                <span>訂單時間</span>

                <strong>

                  {selectedOrder.orderTime
                    ? new Date(
                        selectedOrder.orderTime
                      ).toLocaleString()
                    : '-'}

                </strong>

              </div>


              <div>

                <span>
                  {selectedOrder.shippingFee == null
                  ? '訂單金額'
                  : '訂單總金額（含運費）'}
                </span>

                <strong>
                 {displayMoney(selectedOrder.totalPrice)}
                </strong>

              </div>

            </div>

            {/* 配送、收件與付款資訊 */}
<section className="order-delivery-info">
  <h3>配送與付款資訊</h3>

  <div className="order-summary">
    <div>
      <span>配送方式</span>
      <strong>
        {DELIVERY_LABELS[selectedOrder.deliveryMethod] ?? '未提供'}
      </strong>
    </div>

    <div>
      <span>收件人姓名</span>
      <strong>
        {selectedOrder.recipientName || '未提供'}
      </strong>
    </div>

    <div>
      <span>收件人手機</span>
      <strong>
        {selectedOrder.recipientPhone || '未提供'}
      </strong>
    </div>

    <div>
      <span>付款方式</span>
      <strong>
        {PAYMENT_LABELS[selectedOrder.paymentMethod] ?? '未提供'}
      </strong>
    </div>

    <div>
      <span>付款狀態</span>
      <strong>
        {PAYMENT_STATUS_LABELS[selectedOrder.paymentStatus] ?? '未提供'}
      </strong>
    </div>

    <div>
      <span>商品小計</span>
      <strong>
        {displayMoney(selectedOrder.subtotal)}
      </strong>
    </div>

    <div>
      <span>運費</span>
      <strong>
        {displayMoney(selectedOrder.shippingFee)}
      </strong>
    </div>
  </div>

  {/* 黑貓宅配 */}
  {selectedOrder.deliveryMethod === 'HOME_DELIVERY' && (
    <p>
      <strong>配送地址：</strong>
      {selectedOrder.deliveryAddress || '未提供'}
    </p>
  )}

  {/* 超商取貨 */}
  {['SEVEN_ELEVEN', 'FAMILY_MART'].includes(
    selectedOrder.deliveryMethod
  ) && (
    <div>
      <p>
        <strong>取貨門市：</strong>
        {selectedOrder.pickupStoreName || '未提供'}
      </p>

      <p>
        <strong>門市代碼：</strong>
        {selectedOrder.pickupStoreCode || '未提供'}
      </p>
    </div>
  )}

  {/* 到店取貨 */}
  {selectedOrder.deliveryMethod === 'STORE_PICKUP' && (
    <p>
      <strong>取貨日期：</strong>
      {selectedOrder.pickupDate || '未提供'}
    </p>
  )}

  {selectedOrder.paymentMethod === 'BANK_TRANSFER'
    && selectedOrder.paymentStatus === 'PENDING' && (
      <p>此訂單尚待確認收款，送出訂單不代表已完成付款。</p>
  )}
</section>


            {/* 商品明細 */}
            <div className="order-items-table">


              <div className="order-items-row order-items-header">

                <div>商品編號</div>

                <div>商品名稱</div>

                <div>商品價格</div>

                <div>數量</div>

              </div>


              {orderItems.map((item, index) => (

                <div
                  className="order-items-row"
                  key={item.id ?? index}
                >

                  <div>
                    {item.pid}
                  </div>


                  <div className="order-product-name">
                    {item.productTitle}
                  </div>


                  <div>
                    NT$ {item.productPrice}
                  </div>


                  <div>
                    {item.quantity}
                  </div>

                </div>

              ))}

            </div>


            {orderItems.length === 0 && (

              <div className="empty-order-items">
                此訂單目前沒有商品明細
              </div>

            )}

             {/* 評論區塊 */}
            <OrderReviewForm
              key={`${username}-${selectedOrder.id}`}
              orderId={selectedOrder.id}
            />

          </section>

        )}

      </section>

    </main>
  )
}

export default Orders