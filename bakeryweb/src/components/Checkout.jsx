import { useRef, useState } from 'react'
import { submitOrder } from '../api/ApiService'

const DELIVERY_OPTIONS = {
  HOME_DELIVERY: {
    label: '黑貓宅配',
    fee: 140,
    payments: ['BANK_TRANSFER']
  },
  SEVEN_ELEVEN: {
    label: '7-11 取貨',
    fee: 60,
    payments: ['CASH_ON_DELIVERY', 'BANK_TRANSFER']
  },
  FAMILY_MART: {
    label: '全家取貨',
    fee: 60,
    payments: ['CASH_ON_DELIVERY', 'BANK_TRANSFER']
  },
  STORE_PICKUP: {
    label: '到店取貨',
    fee: 0,
    payments: ['BANK_TRANSFER', 'CASH_AT_STORE']
  }
}

const PAYMENT_LABELS = {
  BANK_TRANSFER: '銀行轉帳',
  CASH_ON_DELIVERY: '貨到付款',
  CASH_AT_STORE: '到店現金付款'
}

// 尚未提供實際收款帳號，先使用明確的占位文字
const TRANSFER_INFO = {
  bank: '待設定',
  accountName: '待設定',
  accountNumber: '待設定'
}

function getTaipeiToday() {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Asia/Taipei',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(new Date())

  const get = type => parts.find(part => part.type === type).value

  return `${get('year')}-${get('month')}-${get('day')}`
}

function Checkout({
  cart,
  isLoggedIn,
  onBack,
  onLogin,
  onSuccess
}) {
  const [form, setForm] = useState({
    deliveryMethod: '',
    recipientName: '',
    recipientPhone: '',
    deliveryAddress: '',
    pickupStoreName: '',
    pickupStoreCode: '',
    pickupDate: '',
    paymentMethod: ''
  })

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  // 避免快速連點造成同一個元件重複送出
  const submittingRef = useRef(false)

  const delivery = DELIVERY_OPTIONS[form.deliveryMethod]

  const isHomeDelivery =
    form.deliveryMethod === 'HOME_DELIVERY'

  const isConvenienceStore =
    form.deliveryMethod === 'SEVEN_ELEVEN'
    || form.deliveryMethod === 'FAMILY_MART'

  const isStorePickup =
    form.deliveryMethod === 'STORE_PICKUP'

  const subtotal = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  )

  const shippingFee = delivery?.fee ?? 0
  const total = subtotal + shippingFee

  function formatMoney(value) {
    return Number(value).toLocaleString('zh-TW')
  }

  function updateField(event) {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))

    setError('')
  }

  function changeDelivery(event) {
    const deliveryMethod = event.target.value

    // 配送方式改變時，清除不再適用的資料與付款選擇
    setForm(previous => ({
      ...previous,
      deliveryMethod,
      deliveryAddress: '',
      pickupStoreName: '',
      pickupStoreCode: '',
      pickupDate: '',
      paymentMethod: ''
    }))

    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (submittingRef.current) return

    setError('')

    if (!isLoggedIn) {
      setError('請先登入後再結帳')
      return
    }

    if (cart.length === 0) {
      setError('購物車是空的')
      return
    }

    if (!delivery) {
      setError('請選擇配送方式')
      return
    }

    if (!delivery.payments.includes(form.paymentMethod)) {
      setError('請選擇此配送方式支援的付款方式')
      return
    }

    if (!form.recipientName.trim()) {
      setError('請填寫收件人姓名')
      return
    }

    if (!/^09\d{8}$/.test(form.recipientPhone.trim())) {
      setError('手機號碼需為 09 開頭的 10 碼數字')
      return
    }

    if (isHomeDelivery && !form.deliveryAddress.trim()) {
      setError('請填寫配送地址')
      return
    }

    if (
      isConvenienceStore
      && (!form.pickupStoreName.trim() || !form.pickupStoreCode.trim())
    ) {
      setError('請填寫超商門市名稱及代碼')
      return
    }

    if (
      isStorePickup
      && (!form.pickupDate || form.pickupDate < getTaipeiToday())
    ) {
      setError('請選擇今天或未來的取貨日期')
      return
    }

    submittingRef.current = true
    setSubmitting(true)

    let order

    try {
      order = await submitOrder(cart, form)
    } catch (err) {
      setError(err.message || '訂單送出失敗')
      submittingRef.current = false
      setSubmitting(false)
      return
    }

    // 成功後由 App 清空購物車及切換頁面
    // 與 API 例外分開，避免畫面切換問題被當成送單失敗
    onSuccess(order)
  }

  if (!isLoggedIn) {
    return (
      <main className="checkout-page">
        <h1>結帳</h1>
        <p>請先登入後再結帳。</p>

        <button type="button" onClick={onLogin}>
          前往登入
        </button>

        <button type="button" onClick={onBack}>
          返回購物車
        </button>
      </main>
    )
  }

  if (cart.length === 0) {
    return (
      <main className="checkout-page">
        <h1>結帳</h1>
        <p>購物車目前沒有商品。</p>

        <button type="button" onClick={onBack}>
          返回購物車
        </button>
      </main>
    )
  }

  return (
    <main className="checkout-page">
      <h1>結帳</h1>

      {/* 商品明細 */}
      <section className="checkout-products">
        <h2>訂購商品</h2>

        <ul>
          {cart.map(item => (
            <li key={item.id}>
              {item.title} × {item.quantity}
              {' — '}
              NT$ {formatMoney(item.price * item.quantity)}
            </li>
          ))}
        </ul>
      </section>

      <form onSubmit={handleSubmit}>
        <fieldset disabled={submitting}>
          <legend>配送與付款資訊</legend>

          {/* 配送方式 */}
          <div className="checkout-field">
            <label htmlFor="deliveryMethod">配送方式</label>

            <select
              id="deliveryMethod"
              name="deliveryMethod"
              value={form.deliveryMethod}
              onChange={changeDelivery}
              required
            >
              <option value="">請選擇配送方式</option>

              {Object.entries(DELIVERY_OPTIONS).map(([value, option]) => (
                <option key={value} value={value}>
                  {option.label}
                  {option.fee === 0
                    ? '（免運）'
                    : `（運費 NT$ ${option.fee}）`}
                </option>
              ))}
            </select>
          </div>

          {/* 收件人 */}
          <div className="checkout-field">
            <label htmlFor="recipientName">收件人姓名</label>

            <input
              id="recipientName"
              name="recipientName"
              value={form.recipientName}
              onChange={updateField}
              autoComplete="name"
              maxLength={100}
              required
            />
          </div>

          <div className="checkout-field">
            <label htmlFor="recipientPhone">收件人手機</label>

            <input
              id="recipientPhone"
              name="recipientPhone"
              type="tel"
              value={form.recipientPhone}
              onChange={updateField}
              autoComplete="tel"
              inputMode="numeric"
              pattern="09[0-9]{8}"
              maxLength={10}
              title="請輸入 09 開頭的 10 碼手機號碼"
              required
            />
          </div>

          {/* 宅配地址 */}
          {isHomeDelivery && (
            <div className="checkout-field">
              <label htmlFor="deliveryAddress">配送地址</label>

              <input
                id="deliveryAddress"
                name="deliveryAddress"
                value={form.deliveryAddress}
                onChange={updateField}
                autoComplete="street-address"
                maxLength={500}
                placeholder="請填寫縣市、區域、路名及門牌"
                required
              />
            </div>
          )}

          {/* 超商資訊 */}
          {isConvenienceStore && (
            <>
              <div className="checkout-field">
                <label htmlFor="pickupStoreName">取貨門市名稱</label>

                <input
                  id="pickupStoreName"
                  name="pickupStoreName"
                  value={form.pickupStoreName}
                  onChange={updateField}
                  maxLength={100}
                  placeholder="請填寫所選超商品牌的門市名稱"
                  required
                />
              </div>

              <div className="checkout-field">
                <label htmlFor="pickupStoreCode">門市代碼</label>

                <input
                  id="pickupStoreCode"
                  name="pickupStoreCode"
                  value={form.pickupStoreCode}
                  onChange={updateField}
                  maxLength={20}
                  required
                />
              </div>
            </>
          )}

          {/* 到店日期 */}
          {isStorePickup && (
            <div className="checkout-field">
              <label htmlFor="pickupDate">取貨日期</label>

              <input
                id="pickupDate"
                name="pickupDate"
                type="date"
                value={form.pickupDate}
                onChange={updateField}
                min={getTaipeiToday()}
                required
              />
            </div>
          )}

          {/* 付款方式 */}
          {delivery && (
            <div className="checkout-field">
              <label htmlFor="paymentMethod">付款方式</label>

              <select
                id="paymentMethod"
                name="paymentMethod"
                value={form.paymentMethod}
                onChange={updateField}
                required
              >
                <option value="">請選擇付款方式</option>

                {delivery.payments.map(method => (
                  <option key={method} value={method}>
                    {PAYMENT_LABELS[method]}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* 轉帳資訊 */}
          {form.paymentMethod === 'BANK_TRANSFER' && (
            <section className="checkout-transfer">
              <h2>銀行轉帳資訊</h2>
              <p>銀行／分行：{TRANSFER_INFO.bank}</p>
              <p>戶名：{TRANSFER_INFO.accountName}</p>
              <p>帳號：{TRANSFER_INFO.accountNumber}</p>
              <p>測試用：收款帳號尚未設定，請勿匯款。</p>
              <p>送出訂單不代表已付款，需待確認收款。</p>
              <p>確認收款後3-5個工作天內寄出。</p>
            </section>
          )}

          {/* 總金額 */}
          <section className="checkout-summary" aria-live="polite">
            <h2>金額確認</h2>

            <p>商品小計：NT$ {formatMoney(subtotal)}</p>

            <p>
              運費：
              {delivery
                ? `NT$ ${formatMoney(shippingFee)}`
                : '請先選擇配送方式'}
            </p>

            <p>
              <strong>
                預估總金額：
                {delivery
                  ? `NT$ ${formatMoney(total)}`
                  : '請先選擇配送方式'}
              </strong>
            </p>

            <p>實際金額以送出時後端核算的訂單金額為準。</p>
          </section>

          <div className="checkout-actions">
            <button type="button" onClick={onBack}>
              返回購物車
            </button>

            <button type="submit">
              {submitting ? '訂單送出中…' : '確認送出訂單'}
            </button>
          </div>
        </fieldset>

        {error && <p role="alert">{error}</p>}
      </form>
    </main>
  )
}

export default Checkout