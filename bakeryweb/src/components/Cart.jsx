


function Cart({
  cart,
  removeFromCart,
  clearCart,
  updateCartQuantity,
  onCheckout,
  isLoggedIn
}) {

  // 計算購物車總金額
  const total = cart.reduce(
    (sum, item) =>
      sum + item.price * item.quantity,
    0
  )


 


  return (

    <main className="cart-page">


      {/* 頁面標題 */}
      <section className="cart-heading">

        <p className="cart-subtitle">
          SHOPPING CART
        </p>

        <h1>購物車</h1>

        <div className="title-line"></div>

      </section>


      <section className="cart-container">


        {/* 購物車沒有商品 */}
        {cart.length === 0 ? (

          <div className="empty-cart">

            <p>您的購物車目前沒有商品。</p>

          </div>

        ) : (

          <>
            {/* 商品表頭 */}
            <div className="cart-table">

              <div className="cart-row cart-header">

                <div>商品</div>

                <div>單價</div>

                <div>數量</div>

                <div>小計</div>

                <div> </div>

              </div>


              {/* 商品列表 */}
              {cart.map((item, index) => (

                <div
                  className="cart-row"
                  key={`${item.id}-${index}`}
                >

                  {/* 商品 */}
                  <div className="cart-product">

                    <img
                      src={`/api/products/${item.id}/image`}
                      alt={item.title}
                      loading="lazy"
                    />

                    <span>
                      {item.title}
                    </span>

                  </div>


                  {/* 單價 */}
                  <div>
                    NT$ {item.price}
                  </div>


                  {/* 數量 */}
                  <div className="cart-quantity">
                    <button
                     type="button"
                      onClick={() => {
                        if (item.quantity === 1) {
                          if (window.confirm('確定要移除該商品')) {
                            removeFromCart(index)
                          }
                          return
                        }

                        updateCartQuantity(item.id, -1)
                      }}
                    >
                      −
                    </button>

                    <span>
                      {item.quantity}
                    </span>

                    <button
                    onClick={() => updateCartQuantity(item.id, 1)}
                    >
                      ＋
                    </button>
                  </div>


                  {/* 小計 */}
                  <div className="cart-subtotal">

                    NT$ {
                      item.price *
                      item.quantity
                    }

                  </div>


                  {/* 刪除 */}
                  <div>

                    <button
                      type="button"
                      className="cart-remove-button"
                      onClick={() => {
                        if (window.confirm('確定要移除該商品')) {
                          removeFromCart(index)
                        }
                      }}
                    >
                      刪除
                    </button>

                  </div>

                </div>

              ))}

            </div>


            {/* 下方總計區 */}
            <div className="cart-summary">

              <div className="cart-summary-info">

                <span>商品小計（未含運費）</span>

                <strong>
                  NT$ {total}
                </strong>

              </div>


              <div className="cart-actions">

                <button
                  className="cart-clear-button"
                  onClick={clearCart}
                >
                  清空購物車
                </button>


                <button
                  type="button"
                  className="cart-submit-button"
                  onClick={onCheckout}
                >
                  前往結帳
                </button>

              </div>

            </div>


            {!isLoggedIn && (

              <p className="cart-login-reminder">
                ※ 送出訂單前請先登入會員。
              </p>

            )}

          </>

        )}

      </section>

    </main>
  )
}

export default Cart