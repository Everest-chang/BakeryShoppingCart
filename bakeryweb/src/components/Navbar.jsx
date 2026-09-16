function Navbar({
  currentPage,
  setCurrentPage,
  setCategory,
  isLoggedIn,
  username,
  cartCount,
  onLogout,
  isLoggingOut
}) {

  return (

    <header className="bakery-header">

      <div className="bakery-navbar">


        {/* Logo */}
        <button
          className="bakery-logo"
          onClick={() =>
            setCurrentPage('home')
          }
        >

          <img
            src="/images/logo.jpg"
            alt="Bakery Logo"
          />

        </button>


        {/* 選單 */}
        <nav className="bakery-menu">


          <button
            className={
              currentPage === 'home'
                ? 'active'
                : ''
            }
            onClick={() =>
              setCurrentPage('home')
            }
          >
            首頁
          </button>


          
        <div className="nav-product-menu">

          <button
            className={
              currentPage === 'products'
                ? 'active'
                : ''
            }
            onClick={() => {setCategory("all"); setCurrentPage("products")}}>
              商品列表
          </button>

          <div className="product-dropdown">

            <button
              onClick={() => {
                setCategory("麵包");
                setCurrentPage("products");
            }}
            >
              <span>麵包</span>
            </button>

            <button
              onClick={() => {
                setCategory("吐司");
                setCurrentPage("products");
              }}
            >
              <span>吐司</span>
            </button>

            <button
              onClick={() => {
                setCategory("點心");
                setCurrentPage("products");
             }}
            >
              <span>點心</span>
            </button>

            <button
              onClick={() => {
                window.open(
                    "/api/products/report",
                    "_blank"
                );
              }}
            >
              <span>商品清單</span>
            </button>
          </div>
        </div>
          
            {isLoggedIn ? (
            <>
              <span className="nav-username">
                {username}
              </span>

              <button
                type="button"
                onClick={onLogout}
                disabled={isLoggingOut}
              >
                {isLoggingOut ? '登出中…' : '登出'}
              </button>
            </>
            ) : (
              <button
                type="button"
                className={
                  currentPage === 'login' ? 'active': ''
                }
               onClick={() => setCurrentPage('login')}
              >
                帳戶登入
              </button>
            )}
        


          <button
            className={
              currentPage === 'orders'
                ? 'active'
                : ''
            }
            onClick={() =>
              setCurrentPage('orders')
            }
          >
            訂單
          </button>

          <button
            type="button"
            className={currentPage === 'reviews' ? 'active' : ''}
            onClick={() => setCurrentPage('reviews')}
          >
            評論
          </button>


          <button
            className={
              currentPage === 'cart'
                ? 'active'
                : ''
            }
            onClick={() =>
              setCurrentPage('cart')
            }
          >

            購物車

            {cartCount > 0 && (

              <span className="cart-count">
                {cartCount}
              </span>

            )}

          </button>

        </nav>

      </div>

    </header>
  )
}

export default Navbar