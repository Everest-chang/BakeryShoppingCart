import { useEffect, useState } from 'react'

import {
  createReview,
  updateReview,
  fetchReviewByOrderId
} from '../api/ApiService'

const MAX_IMAGES = 5
const MAX_FILE_SIZE = 5 * 1024 * 1024

// 本機圖片預覽，移除圖片或卸載時釋放 URL
function LocalImagePreview({ file }) {
  const [url, setUrl] = useState('')

  useEffect(() => {
    const objectUrl = URL.createObjectURL(file)
    setUrl(objectUrl)

    return () => {
      URL.revokeObjectURL(objectUrl)
    }
  }, [file])

  return url ? (
    <img
      src={url}
      alt={file.name}
      width="120"
      height="120"
      style={{ objectFit: 'cover' }}
    />
  ) : null
}

function OrderReviewForm({ orderId }) {
  const [review, setReview] = useState(null)
  const [rating, setRating] = useState(0)
  const [content, setContent] = useState('')

  // 資料庫中要保留的圖片
  const [existingImages, setExistingImages] = useState([])

  // 本次新選擇的檔案
  const [newImages, setNewImages] = useState([])

  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [hasConflict, setHasConflict] = useState(false)

  // 載入這張訂單的既有評論
  useEffect(() => {
    let active = true

    async function loadReview() {
      setLoading(true)
      setLoadError('')
      setError('')
      setSuccess('')
      setHasConflict(false)

      try {
        const data = await fetchReviewByOrderId(orderId)

        if (!active) return

        setReview(data)
        setRating(data?.rating ?? 0)
        setContent(data?.content ?? '')
        setExistingImages(data?.images ?? [])
        setNewImages([])
      } catch (err) {
        if (active) {
          setLoadError(err.message || '載入評論失敗')
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadReview()

    return () => {
      active = false
    }
  }, [orderId, reloadCount])

  function handleSelectImages(event) {
    const selected = Array.from(event.target.files ?? [])

    // 允許之後重新選取相同檔案
    event.target.value = ''

    if (selected.length === 0) return

    setError('')
    setSuccess('')

    const total =
      existingImages.length + newImages.length + selected.length

    if (total > MAX_IMAGES) {
      setError('保留圖片與新增圖片合計最多 5 張')
      return
    }

    for (const file of selected) {
      if (!['image/jpeg', 'image/png'].includes(file.type)) {
        setError(`${file.name}：只支援 JPEG 或 PNG`)
        return
      }

      if (file.size === 0) {
        setError(`${file.name}：不能上傳空檔案`)
        return
      }

      if (file.size > MAX_FILE_SIZE) {
        setError(`${file.name}：單張圖片最多 5 MB`)
        return
      }
    }

    setNewImages(previous => [...previous, ...selected])
  }

  async function handleSubmit(event) {
    event.preventDefault()

    if (submitting || hasConflict) return

    setError('')
    setSuccess('')

    if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
      setError('請選擇 1～5 顆星的評分')
      return
    }

    if (Array.from(content.trim()).length > 2000) {
      setError('評論文字最多 2,000 字')
      return
    }

    if (existingImages.length + newImages.length > MAX_IMAGES) {
      setError('圖片合計最多 5 張')
      return
    }

    setSubmitting(true)

    try {
      let result

      if (review) {
        result = await updateReview(review.id, {
          version: review.version,
          rating,
          content,
          retainedImageIds: existingImages.map(image => image.id),
          images: newImages
        })
      } else {
        result = await createReview({
          orderId,
          rating,
          content,
          images: newImages
        })
      }

      // 成功後使用後端回傳的版本與圖片資料
      setReview(result)
      setRating(result.rating)
      setContent(result.content)
      setExistingImages(result.images ?? [])
      setNewImages([])

      setSuccess('評論已儲存')
    } catch (err) {
      setError(err.message || '評論儲存失敗')

      if (err.status === 409) {
        setHasConflict(true)
      }

      // 失敗保留本次輸入，讓使用者修正
    } finally {
      setSubmitting(false)
    }
  }

  function reloadReview() {
    const confirmed = window.confirm(
      '重新載入會捨棄目前尚未儲存的文字、評分與圖片變更，是否繼續？'
    )

    if (confirmed) {
      setReloadCount(previous => previous + 1)
    }
  }

  if (loading) {
    return <p role="status">正在載入評論…</p>
  }

  if (loadError) {
    return (
      <section className="order-review">
        <p role="alert">{loadError}</p>

        <button
          type="button"
          onClick={() => setReloadCount(previous => previous + 1)}
        >
          重新載入評論
        </button>
      </section>
    )
  }

  return (
    <section className="order-review">
      <h2>{review ? '修改訂單評論' : '為此訂單評論'}</h2>

      <form onSubmit={handleSubmit}>
        <fieldset
          disabled={submitting || hasConflict}
          style={{ border: 0, padding: 0, margin: 0 }}
        >
          <legend>評論內容</legend>

          {/* 星等 */}
          <fieldset>
           <legend>評分（必填）</legend>

            <div className="review-stars">
              {[1, 2, 3, 4, 5].map(value => (
                <label className="review-star" key={value}>
                  <input
                    type="radio"
                    name={`review-rating-${orderId}`}
                    value={value}
                    checked={rating === value}
                    onChange={() => setRating(value)}
                    required
                    aria-label={`${value} 星`}
                  />

                  <span
                    aria-hidden="true"
                    className={value <= rating ? 'selected' : ''}
                  >
                    {value <= rating ? '★' : '☆'}
                  </span>
                </label>
              ))}
            </div>

            <p>
              {rating ? `目前評分：${rating} 星` : '尚未選擇評分'}
            </p>
          </fieldset>

          {/* 文字 */}
          <div>
            <label htmlFor={`review-content-${orderId}`}>
              評論文字（選填）
            </label>

            <textarea
              id={`review-content-${orderId}`}
              value={content}
              onChange={event => setContent(event.target.value)}
              rows={5}
              placeholder="分享這次訂購的心得"
              style={{ display: 'block', width: '100%' }}
            />

            <p>{Array.from(content).length} / 2000 字</p>
          </div>

          {/* 圖片 */}
          <div>
            <label htmlFor={`review-images-${orderId}`}>
              上傳圖片（選填）
            </label>

            <input
              id={`review-images-${orderId}`}
              type="file"
              accept="image/jpeg,image/png"
              multiple
              onChange={handleSelectImages}
            />

            <p>
              JPEG、PNG，每張最多 5 MB，合計最多 5 張。
              目前 {existingImages.length + newImages.length} 張。
            </p>
          </div>

          {/* 既有圖片 */}
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: '12px'
            }}
          >
            {existingImages.map(image => (
              <div key={image.id}>
                <img
                  src={image.url}
                  alt="已上傳的評論圖片"
                  width="120"
                  height="120"
                  style={{ objectFit: 'cover' }}
                />

                <button
                  type="button"
                  onClick={() => {
                    setExistingImages(previous =>
                      previous.filter(item => item.id !== image.id)
                    )
                    setSuccess('')
                  }}
                >
                  移除此圖片
                </button>
              </div>
            ))}

            {/* 新選圖片 */}
            {newImages.map((file, index) => (
              <div key={`${file.name}-${file.lastModified}-${index}`}>
                <LocalImagePreview file={file} />

                <button
                  type="button"
                  onClick={() => {
                    setNewImages(previous =>
                      previous.filter((_, i) => i !== index)
                    )
                    setSuccess('')
                  }}
                >
                  移除新圖片
                </button>
              </div>
            ))}
          </div>

          <button type="submit">
            {submitting
              ? '儲存中…'
              : review
                ? '儲存修改'
                : '送出評論'}
          </button>
        </fieldset>

        {error && <p role="alert">{error}</p>}
        {success && <p role="status">{success}</p>}

        {hasConflict && (
          <button type="button" onClick={reloadReview}>
            捨棄變更並載入最新評論
          </button>
        )}
      </form>
    </section>
  )
}

export default OrderReviewForm