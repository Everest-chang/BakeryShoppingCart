import { useEffect, useState } from 'react'
import { fetchReviews } from '../api/ApiService'

function Reviews() {
  // 搜尋框正在輸入的文字
  const [searchInput, setSearchInput] = useState('')

  // 實際送到後端的搜尋條件
  const [query, setQuery] = useState({
    keyword: '',
    page: 0
  })

  const [reviews, setReviews] = useState([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)

  useEffect(() => {
    let active = true

    async function loadReviews() {
      setLoading(true)
      setError('')

      try {
        const result = await fetchReviews({
          keyword: query.keyword,
          page: query.page,
          size: 10
        })

        if (!active) return

        setReviews(result.content)
        setTotalPages(result.totalPages)
        setTotalElements(result.totalElements)
      } catch (err) {
        if (active) {
          setError(err.message || '載入評論失敗')
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadReviews()

    return () => {
      active = false
    }
  }, [query, reloadCount])

  function handleSearch(event) {
    event.preventDefault()

    // 更換關鍵字時回到第一頁
    setQuery({
      keyword: searchInput.trim(),
      page: 0
    })
  }

  function showAllReviews() {
    setSearchInput('')

    setQuery({
      keyword: '',
      page: 0
    })
  }

  function changePage(page) {
    setQuery(previous => ({
      ...previous,
      page
    }))

    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }

  function formatTime(value) {
    if (!value) return ''

    return new Date(value).toLocaleString('zh-TW')
  }

  return (
    <main className="reviews-page">
      <section className="reviews-heading">
        <p>CUSTOMER REVIEWS</p>
        <h1>顧客評論</h1>
        <p>看看大家的訂購心得。</p>
      </section>

      {/* 關鍵字搜尋 */}
      <form
        className="reviews-search"
        onSubmit={handleSearch}
      >
        <label htmlFor="review-search">
          搜尋評論或商品名稱
        </label>

        <input
          id="review-search"
          type="search"
          value={searchInput}
          onChange={event => setSearchInput(event.target.value)}
          placeholder="例如：吐司、好吃"
          maxLength={100}
        />

        <button type="submit">
          搜尋
        </button>

        <button
          type="button"
          onClick={showAllReviews}
        >
          全部評論
        </button>
      </form>

      {loading ? (
        <p role="status">正在載入評論…</p>
      ) : error ? (
        <div>
          <p role="alert">{error}</p>

          <button
            type="button"
            onClick={() => setReloadCount(previous => previous + 1)}
          >
            重試
          </button>
        </div>
      ) : (
        <>
          <p>
            {query.keyword
              ? `「${query.keyword}」的搜尋結果`
              : '全部評論'}
            ：共 {totalElements} 則
          </p>

          {reviews.length === 0 ? (
            <p>
              {query.keyword
                ? '沒有符合關鍵字的評論。'
                : '目前還沒有評論。'}
            </p>
          ) : (
            <div className="reviews-list">
              {reviews.map(review => (
                <article
                  key={review.id}
                  className="review-card"
                >
                  <header>
                    {/* 帳號已由後端匿名處理 */}
                    <strong>{review.displayUsername}</strong>

                    <div
                      role="img"
                      aria-label={`評分 ${review.rating} 顆星，滿分 5 顆星`}
                    >
                      <span
                        aria-hidden="true"
                        style={{
                          color: '#b87500',
                          fontSize: '24px'
                        }}
                      >
                        {'★'.repeat(review.rating)}
                        {'☆'.repeat(5 - review.rating)}
                      </span>
                    </div>

                    <p>
                      發表於{' '}
                      <time dateTime={review.createdAt}>
                        {formatTime(review.createdAt)}
                      </time>
                    </p>

                    {review.updatedAt && (
                      <p>
                        已編輯：{' '}
                        <time dateTime={review.updatedAt}>
                          {formatTime(review.updatedAt)}
                        </time>
                      </p>
                    )}
                  </header>

                  {/* 訂單購買的商品 */}
                  <div className="review-products">
                    <h2>購買商品</h2>

                    <ul>
                      {(review.products ?? []).map(product => (
                        <li
                          key={`${product.productId}-${product.productTitle}`}
                        >
                          {product.productTitle}
                        </li>
                      ))}
                    </ul>
                  </div>

                  {/* 評論文字 */}
                  <p style={{ whiteSpace: 'pre-wrap' }}>
                    {review.content || '此評論未填寫文字。'}
                  </p>

                  {/* 評論圖片 */}
                  <div
                    className="review-images"
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: '12px'
                    }}
                  >
                    {(review.images ?? []).map((image, index) => (
                      <a
                        key={image.id}
                        href={image.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        aria-label={`開啟第 ${index + 1} 張評論圖片`}
                      >
                        <img
                          src={image.url}
                          alt={`評論附圖 ${index + 1}`}
                          loading="lazy"
                          width="140"
                          height="140"
                          style={{ objectFit: 'cover' }}
                        />
                      </a>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          )}

          {/* 分頁 */}
          {totalPages > 1 && (
            <nav
              className="reviews-pagination"
              aria-label="評論分頁"
            >
              <button
                type="button"
                disabled={query.page === 0}
                onClick={() => changePage(query.page - 1)}
              >
                上一頁
              </button>

              <span>
                第 {query.page + 1} / {totalPages} 頁
              </span>

              <button
                type="button"
                disabled={query.page >= totalPages - 1}
                onClick={() => changePage(query.page + 1)}
              >
                下一頁
              </button>
            </nav>
          )}
        </>
      )}
    </main>
  )
}

export default Reviews