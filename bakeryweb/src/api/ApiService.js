const BASE_URL = '/api'

// Access Token 只存在記憶體
let accessToken = null

// 多個 API 同時需要更新時，共用同一次請求
let refreshPromise = null

// =========================
// 登入
// =========================
export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/user/login`, {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  })

  if (!res.ok) {
    throw new Error('帳號或密碼錯誤')
  }

  const data = await res.json()

  accessToken = data.accessToken

  return data
}

// =========================
// 註冊
// =========================
export async function register(username, password, name, email) {
  const res = await fetch(`${BASE_URL}/user/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      username,
      password,
      name,
      email
    })
  })

  const message = await res.text()

  if (!res.ok) {
    throw new Error(message || '會員建立失敗')
  }

  return message
}

// =========================
// 更新長短 token
// =========================
export async function refreshLogin() {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const res = await fetch(`${BASE_URL}/user/refresh`, {
        method: 'POST',
        credentials: 'same-origin'
      })

      if (!res.ok) {
        if (res.status === 401) {
          accessToken = null
        }

        throw new Error(
          res.status === 401
            ? '登入已失效，請重新登入'
            : '更新登入失敗，請稍後再試'
        )
      }

      const data = await res.json()

      accessToken = data.accessToken

      return data
    })()
  }

  const pendingRefresh = refreshPromise

  try {
    return await pendingRefresh
  } finally {
    if (refreshPromise === pendingRefresh) {
      refreshPromise = null
    }
  }
}

// =========================
// 需要登入的 API 共用入口
// =========================
async function authFetch(path, options = {}) {
  // 若有更新正在進行，等待完成
  if (refreshPromise) {
    await refreshPromise
  }

  // 重新整理後，記憶體沒有 Access Token
  if (!accessToken) {
    await refreshLogin()
  }

  function sendRequest(token) {
    const headers = new Headers(options.headers)

    headers.set('Authorization', `Bearer ${token}`)

    return fetch(`${BASE_URL}${path}`, {
      ...options,
      credentials: 'same-origin',
      headers
    })
  }

  const tokenUsed = accessToken
  let res = await sendRequest(tokenUsed)

  if (res.status === 401) {
    // 若其他請求已經更新 token，就直接使用新的
    if (accessToken === tokenUsed || !accessToken) {
      await refreshLogin()
    }

    // 原請求最多重試一次
    res = await sendRequest(accessToken)

    if (res.status === 401) {
      accessToken = null
      throw new Error('登入已失效，請重新登入')
    }
  }

  return res
}

// =========================
// 登出
// =========================
export async function logout() {
  // 避免登出時，先前的 refresh 還在更新 Cookie
  if (refreshPromise) {
    try {
      await refreshPromise
    } catch {
      // 即使更新失敗，仍嘗試呼叫登出
    }
  }

  const res = await fetch(`${BASE_URL}/user/logout`, {
    method: 'POST',
    credentials: 'same-origin'
  })

  if (!res.ok) {
    throw new Error('登出失敗，請稍後再試')
  }

  accessToken = null

  // 後端回傳 204，沒有 JSON 可以解析
}

// =========================
// 商品
// =========================
export async function fetchProducts() {
  const res = await fetch(`${BASE_URL}/products`)

  if (!res.ok) {
    throw new Error('載入產品失敗')
  }

  return res.json()
}

export async function fetchProductsByCategory(category) {
  const res = await fetch(
    `${BASE_URL}/products/category/${encodeURIComponent(category)}`
  )

  if (!res.ok) {
    throw new Error('取得商品分類失敗')
  }

  return res.json()
}

// =========================
// 訂單
// =========================
export async function fetchOrders(username) {
  const res = await authFetch(
    `/orders/${encodeURIComponent(username)}`
  )

  if (!res.ok) {
    throw new Error('載入訂單失敗')
  }

  return res.json()
}

export async function fetchOrderById(orderId) {
  const res = await authFetch(`/orders/orderid/${orderId}`)

  if (!res.ok) {
    throw new Error('載入訂單詳情失敗')
  }

  return res.json()
}

export async function fetchOrderItems(orderId) {
  const res = await authFetch(`/items/${orderId}`)

  if (!res.ok) {
    throw new Error('載入商品明細失敗')
  }

  return res.json()
}

// 結帳並建立訂單
export async function submitOrder(cart, checkout) {
  const request = {
    // 只傳商品編號和數量，不傳價格或總金額
    items: cart.map(item => ({
      productId: item.id,
      quantity: Number(item.quantity)
    })),

    deliveryMethod: checkout.deliveryMethod,

    recipientName: checkout.recipientName.trim(),
    recipientPhone: checkout.recipientPhone.trim(),

    // 只傳送所選配送方式需要的資料
    deliveryAddress:
      checkout.deliveryMethod === 'HOME_DELIVERY'
        ? checkout.deliveryAddress.trim()
        : null,

    pickupStoreName:
      ['SEVEN_ELEVEN', 'FAMILY_MART'].includes(
        checkout.deliveryMethod
      )
        ? checkout.pickupStoreName.trim()
        : null,

    pickupStoreCode:
      ['SEVEN_ELEVEN', 'FAMILY_MART'].includes(
        checkout.deliveryMethod
      )
        ? checkout.pickupStoreCode.trim()
        : null,

    pickupDate:
      checkout.deliveryMethod === 'STORE_PICKUP'
        ? checkout.pickupDate
        : null,

    paymentMethod: checkout.paymentMethod
  }

  const res = await authFetch('/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  })

  if (!res.ok) {
    let message = '訂單送出失敗，請稍後再試'

    try {
      const errorData = await res.json()
      message = errorData.message || message
    } catch {
      // 後端未回傳 JSON 時，保留預設訊息
    }

    const error = new Error(message)
    error.status = res.status
    throw error
  }

  return res.json()
}

// =========================
// 商品 PDF 清單
// =========================
export async function openProductReport() {
  const res = await fetch(`${BASE_URL}/products/report`)

  if (!res.ok) {
    throw new Error('商品目錄開啟失敗')
  }

  const blob = await res.blob()
  const pdfUrl = window.URL.createObjectURL(blob)

  window.open(pdfUrl, '_blank')

  setTimeout(() => {
    window.URL.revokeObjectURL(pdfUrl)
  }, 60_000)
}

// =========================
// 解析評論 API 回應
// =========================
async function readReviewResponse(res) {
  if (!res.ok) {
    let message = '評論操作失敗，請稍後再試'

    try {
      const error = await res.json()
      message = error.message || message
    } catch {
      // 回應不是 JSON 時，使用預設訊息
    }

    const error = new Error(message)
    error.status = res.status
    throw error
  }

  // 尚未評論時，後端回傳 204
  if (res.status === 204) {
    return null
  }

  return res.json()
}

// =========================
// 建立 multipart 請求內容
// =========================
function buildReviewFormData(review, images = []) {
  const formData = new FormData()

  // 對應後端 @RequestPart("review")
  formData.append(
    'review',
    new Blob(
      [JSON.stringify(review)],
      { type: 'application/json' }
    )
  )

  // 對應後端 @RequestPart("images")
  for (const file of images) {
    formData.append('images', file)
  }

  return formData
}

// =========================
// 新增評論，需要登入
// =========================
export async function createReview({
  orderId,
  rating,
  content,
  images = []
}) {
  const formData = buildReviewFormData(
    {
      orderId,
      rating,
      content
    },
    images
  )

  const res = await authFetch('/reviews', {
    method: 'POST',
    body: formData
  })

  return readReviewResponse(res)
}

// =========================
// 修改評論，需要登入
// =========================
export async function updateReview(
  reviewId,
  {
    version,
    rating,
    content,
    retainedImageIds,
    images = []
  }
) {
  const formData = buildReviewFormData(
    {
      version,
      rating,
      content,
      retainedImageIds
    },
    images
  )

  const res = await authFetch(`/reviews/${reviewId}`, {
    method: 'PUT',
    body: formData
  })

  return readReviewResponse(res)
}

// =========================
// 查詢自己的訂單評論
// =========================
export async function fetchReviewByOrderId(orderId) {
  const res = await authFetch(`/reviews/order/${orderId}`)

  return readReviewResponse(res)
}

// =========================
// 公開評論列表與關鍵字搜尋
// =========================
export async function fetchReviews({
  keyword = '',
  page = 0,
  size = 10
} = {}) {
  const params = new URLSearchParams({
    keyword,
    page: String(page),
    size: String(size)
  })

  const res = await fetch(
    `${BASE_URL}/reviews?${params.toString()}`
  )

  return readReviewResponse(res)
}