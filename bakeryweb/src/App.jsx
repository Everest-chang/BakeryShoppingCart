import { useEffect, useState } from 'react'
import { refreshLogin, logout } from './api/ApiService'

import Navbar from './components/Navbar'
import Login from './components/Login'
import Products from './components/Products'
import Cart from './components/Cart'
import Orders from './components/Orders'
import Footer from './components/Footer'
import Register from './components/Register'
import Reviews from './components/Reviews'
import Checkout from './components/Checkout'



import './bakery.css'


function App() {

  // 目前顯示哪一個頁面
  const [currentPage, setCurrentPage] = useState('home')

  const [category, setCategory] = useState("all");

  // 是否已登入
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  // 登入者帳號
  const [username, setUsername] = useState('')

  // 購物車
  const [cart, setCart] = useState([])

  // 網頁載入時，先確認是否能恢復登入
  const [authLoading, setAuthLoading] = useState(true)

  // 記錄恢復登入時的錯誤
  const [authError, setAuthError] = useState('')

  // 重新嘗試恢復登入的次數
  const [authRetry, setAuthRetry] = useState(0)

  const [isLoggingOut, setIsLoggingOut] = useState(false)

  useEffect(() => {
    window.scrollTo({
        top: 0,
        left: 0,
        behavior: "instant"
    });
  }, [currentPage]);
  

  useEffect(() => {
  let active = true

  async function restoreLogin() {
    setAuthLoading(true)
    setAuthError('')

    // 清除舊版登入留下的瀏覽器儲存資料
    localStorage.removeItem('token')
    sessionStorage.removeItem('username')

    try {
      const result = await refreshLogin()

      if (!active) return

      setIsLoggedIn(true)
      setUsername(result.username)
    } catch (error) {
      if (!active) return

      setIsLoggedIn(false)
      setUsername('')

      // 沒有 Cookie 或登入到期，是一般未登入狀態
      // 其他錯誤，例如網路中斷，顯示提示
      if (error.message !== '登入已失效，請重新登入') {
        setAuthError('目前無法確認登入狀態，請重試或前往登入。')
      }
    } finally {
      if (active) {
        setAuthLoading(false)
      }
    }
  }

  restoreLogin()

  return () => {
    active = false
  }
}, [authRetry])

  // =========================
  // 登入成功
  // =========================
function handleLoginSuccess(user) {
  setIsLoggedIn(true)
  setUsername(user)
  setAuthError('')
  setCurrentPage('home')
}

async function handleLogout() {
  if (isLoggingOut) return

  setIsLoggingOut(true)

  try {
    // 撤銷後端 Refresh Token、清除 Cookie 與記憶體 token
    await logout()

    // 清除會員畫面狀態
    setIsLoggedIn(false)
    setUsername('')
    setAuthError('')

    // 清空目前購物車，避免下一個登入者看到前一人的內容
    setCart([])

    // 清除舊版登入資料
    localStorage.removeItem('token')
    sessionStorage.removeItem('username')

    setCurrentPage('home')

    alert('已登出')
  } catch (error) {
    // 後端登出失敗時，不顯示登出成功
    alert(error.message || '登出失敗，請稍後再試')
  } finally {
    setIsLoggingOut(false)
  }
}


  // =========================
  // 加入購物車
  // =========================
  function addToCart(product, quantity) {

    setCart(prev => {

      // 檢查商品是否已經存在購物車
      const exists = prev.find(
        item => item.id === product.id
      )

      // 已存在 → 數量增加
      if (exists) {

        return prev.map(item =>
          item.id === product.id
            ? {
                ...item,
                quantity:
                  item.quantity + Number(quantity)
              }
            : item
        )
      }


      // 不存在 → 新增商品
      return [
        ...prev,
        {
          ...product,
          quantity: Number(quantity)
        }
      ]
    })

    alert(`已將 ${product.title} 加入購物車`)
  }


  // =========================
  // 移除購物車商品
  // =========================
  function removeFromCart(index) {

    setCart(prev =>
      prev.filter(
        (_, i) => i !== index
      )
    )
  }


  // =========================
  // 清空購物車
  // =========================
  function clearCart() {
    setCart([])
  }


  // =========================
  // 購物車總數
  // =========================
  const cartCount = cart.reduce(
    (sum, item) =>
      sum + item.quantity,
    0
  )

  // =========================
  // 購物車數量更改
  // =========================
  function updateCartQuantity(productId, change) {

    setCart(prevCart =>
        prevCart
            .map(item => {

                if (item.id === productId) {

                    return {
                        ...item,
                        quantity: item.quantity + change
                    };
                }

                return item;
            })
            .filter(item => item.quantity > 0)
    );
  } 
  
  if (authLoading) {
  return (
    <main className="page-container">
      <p role="status">正在確認登入狀態…</p>
    </main>
  )
}

if (authError) {
  return (
    <main className="page-container">
      <p role="alert">{authError}</p>

      <button
        onClick={() => {
          setAuthLoading(true)
          setAuthRetry(previous => previous + 1)
        }}
      >
        重試
      </button>

      <button
        onClick={() => {
          setAuthError('')
          setCurrentPage('login')
        }}
      >
        前往登入
      </button>
    </main>
  )
}

function handleGoToCheckout() {
  if (cart.length === 0) {
    alert('購物車是空的！')
    return
  }

  if (!isLoggedIn) {
    alert('請先登入後再結帳')
    setCurrentPage('login')
    return
  }

  setCurrentPage('checkout')
}

function handleCheckoutSuccess(order) {
  // 後端確認建立成功後，才清空購物車
  setCart([])

  alert(
    `訂單 #${order.id} 已送出！\n` +
    `含運費總金額：NT$ ${Number(order.totalPrice).toLocaleString('zh-TW')}\n` +
    '付款狀態：待付款'
  )

  setCurrentPage('orders')
}


  return (
    <>

      {/* 上方固定導覽列 */}
      <Navbar
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        setCategory={setCategory}
        isLoggedIn={isLoggedIn}
        username={username}
        cartCount={cartCount}
        onLogout={handleLogout}
        isLoggingOut={isLoggingOut}
      />


      {/* =========================
          首頁
      ========================= */}

      {currentPage === 'home' && (

        <main className="home-page">

          <section className="home-banner">

            <div className="home-banner-content">

              <p className="home-small-title">
                DAILY BAKERY
              </p>

              <h1>
                每一天，都值得一份麵包
              </h1>

              <p>
                潘媽媽烘焙坊，堅持健康無添加的手作。
              </p>

              <button
                className="home-products-button"
                onClick={() =>
                  setCurrentPage('products')
                }
              >
                查看商品
              </button>

            </div>

          </section>

        </main>

      )}


      {/* =========================
          登入
      ========================= */}

      {/* 登入 */}
      {currentPage === 'login' && (

      <Login
        onLoginSuccess={handleLoginSuccess}
        setCurrentPage={setCurrentPage}
      />

      )}


      {/* 加入會員 */}
      {currentPage === 'register' && (

        <Register
          setCurrentPage={setCurrentPage}
        />

      )}

      {/* =========================
          商品列表
      ========================= */}

      {currentPage === 'products' && (

        <Products
          addToCart={addToCart}
          category={category}
        />

      )}


      {/* =========================
          購物車
      ========================= */}

      {currentPage === 'cart' && (

        <div className="page-container">

          <Cart
            cart={cart}
            removeFromCart={removeFromCart}
            clearCart={clearCart}
            isLoggedIn={isLoggedIn}
            username={username}
            updateCartQuantity={updateCartQuantity}
            onCheckout={handleGoToCheckout}
          />

        </div>

      )}

      {/* 結帳頁 */}
      {currentPage === 'checkout' && (
        <div className="page-container">
          <Checkout
            cart={cart}
            isLoggedIn={isLoggedIn}
            onBack={() => setCurrentPage('cart')}
            onLogin={() => setCurrentPage('login')}
            onSuccess={handleCheckoutSuccess}
          />
       </div>
      )}


      {/* =========================
          訂單
      ========================= */}

      {currentPage === 'orders' && (

        <div className="page-container">

          <Orders
            isLoggedIn={isLoggedIn}
            username={username}
          />

        </div>

      )}

      {/* 公開評論頁 */}
      {currentPage === 'reviews' && (
        <div className="page-container">
          <Reviews />
        </div>
      )}


      {/* 下方 Footer */}
      <Footer />

      </>  
  )

  
}

export default App