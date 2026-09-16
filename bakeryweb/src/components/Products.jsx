import { useState, useEffect } from 'react'
import { fetchProducts, fetchProductsByCategory } from '../api/ApiService'
import { openProductReport } from "../api/ApiService";

function Products({ addToCart, category }) {
  const [products, setProducts] = useState([])
  // 每個產品各自獨立的數量 state，以 productId 為 key
  const [quantities, setQuantities] = useState({})
  const [currentPage, setCurrentPage] = useState(1);
  const productsPerPage = 12;

  // useEffect 替代 jQuery 的 document.ready + loadProducts()
  useEffect(() => {
    const loadProducts = async () => {
        try {
            let data;
            if (category === "all") {
                data = await fetchProducts();
            } else {
                data =await fetchProductsByCategory(category);
            }
      setProducts(data);// 初始化每個產品數量為 1
      setCurrentPage(1);// 每次切換分類，都從第一頁開始
      const initQty = {};
      data.forEach(p => { initQty[p.id] = 1 });
      setQuantities(initQty);
     } catch (error) {
            console.error("取得商品失敗：", error);
        }
    };
    loadProducts();
  }, [category]);

   const indexOfLastProduct = currentPage * productsPerPage;

   const indexOfFirstProduct =
    indexOfLastProduct - productsPerPage;

    const currentProducts = products.slice(
    indexOfFirstProduct,
    indexOfLastProduct
    );

    const totalPages = Math.ceil(
    products.length / productsPerPage
    );

  function handleQtyChange(productId, value) {
    setQuantities(prev => ({ ...prev, [productId]: Number(value) }))
  }

   return (
    <main className="products-page">

      {/* 商品頁標題 */}
      <section className="products-heading">
        <p className="products-subtitle">OUR PRODUCTS</p>
        <h1>商品列表</h1>
        <div className="title-line"></div>
      </section>


      {/* 商品列表 */}
      <section className="products-container">

        <div className="products-grid">

          {currentProducts.map(product => (

            <article
              className="product-card"
              key={product.id}
            >

              {/* 商品圖片 */}
              <div className="product-image-box">

                <img
                  src={`/api/products/${product.id}/image`}
                  alt={product.title}
                  className="product-image"
                  loading="lazy"
                />

              </div>


              {/* 商品名稱 */}
              <h2 className="product-title">
                {product.title}
              </h2>


              {/* 商品價格 */}
              <p className="product-price">
                NT$ {product.price}
              </p>


              {/* 商品數量 */}
              <div className="quantity-area">

                <span>數量</span>

                <input
                  type="number"
                  min="1"
                  value={quantities[product.id] ?? 1}
                  onChange={e =>
                    handleQtyChange(
                      product.id,
                      e.target.value
                    )
                  }
                />

              </div>


              {/* 加入購物車 */}
              <button
                className="add-cart-button"
                onClick={() =>
                  addToCart(
                    product,
                    quantities[product.id] ?? 1
                  )
                }
              >
                加入購物車
              </button>

            </article>

          ))}
        </div>

        {/* 分頁 */}
         <div className="pagination">
           {/* 上一頁 */}
            <button
              className="page-arrow"
              onClick={() => setCurrentPage(currentPage - 1)}
              disabled={currentPage === 1}
            >
              ‹
            </button>
            {/* 頁碼 */}
            {Array.from({ length: totalPages }, (_, index) => {
              const pageNumber = index + 1;

              return (
              <button
                key={pageNumber}
                className={
                  currentPage === pageNumber ? "page-number active" : "page-number"
                }
                onClick={() => {setCurrentPage(pageNumber);
                  window.scrollTo({top: 0, behavior: "smooth"});
                }}
              
              >
                {pageNumber}
              </button>
              );
            })}
            {/* 下一頁 */}
            <button
              className="page-arrow"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              ›
            </button>
          </div>
      </section>
    </main>
  )
}

export default Products
