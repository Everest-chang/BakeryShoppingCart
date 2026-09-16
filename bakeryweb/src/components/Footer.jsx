function Footer() {
  return (
    <footer className="bakery-footer">

      <div className="footer-container">

        <div className="footer-brand">
          <img
            src="/images/logo.jpg"
            alt="Bakery Logo"
          />

          <p>
            每一份甜點，都為日常增添一點幸福。
          </p>
        </div>


        <div className="footer-info">
          <h3>網站選單</h3>

          <a href="/">首頁</a>
          <a href="/products">商品列表</a>
          <a href="/login">帳戶登入</a>
          <a href="/orders">訂單</a>
          <a href="/cart">購物車</a>
        </div>

      </div>


      <div className="footer-bottom">
        Copyright © 2026 Bakery. All Rights Reserved.
      </div>

    </footer>
  )
}

export default Footer