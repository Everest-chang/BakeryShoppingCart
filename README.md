# 潘媽媽烘焙坊購物車專案

本專案以 React 與 Spring Boot 建立前後端分離的烘焙商品購物網站，串接 MySQL 完成會員驗證、商品瀏覽、購物車、配送結帳、訂單查詢及圖片評論。

前端管理操作與畫面狀態；後端驗證登入身分、資料歸屬及輸入內容，決定成交價格與運費；資料庫保存訂單快照、評論、圖片及登入憑證紀錄。本文件著重目前功能的方法與設計邏輯。

## 目錄

1. [專案功能](#1-專案功能)
2. [技術架構與目錄](#2-技術架構與目錄)
3. [前後端通訊與設定](#3-前後端通訊與設定)
4. [網站操作流程](#4-網站操作流程)
5. [會員登入與權限機制](#5-會員登入與權限機制)
6. [商品與購物車邏輯](#6-商品與購物車邏輯)
7. [結帳與訂單邏輯](#7-結帳與訂單邏輯)
8. [評論圖片與交易](#8-評論圖片與交易)
9. [評論搜尋與後端分頁](#9-評論搜尋與後端分頁)
10. [商品圖片與報表](#10-商品圖片與報表)
11. [資料表與-api](#11-資料表與-api)
12. [核心設計整理](#12-核心設計整理)

## 1. 專案功能

| 功能 | 說明 |
| --- | --- |
| 商品瀏覽 | 商品列表、分類、每頁 12 筆的前端分頁、商品圖片 |
| 會員功能 | 註冊、登入、登出、短效 JWT 與 Refresh Token 輪替 |
| 購物車 | 加入商品、合併相同商品、調整數量、小計與移除確認 |
| 配送結帳 | 黑貓宅配、7-11、全家、到店取貨及對應資料驗證 |
| 付款選擇 | 依配送方式提供銀行轉帳、貨到付款或到店現金 |
| 訂單查詢 | 本人訂單、商品明細、成交金額、配送及付款資訊 |
| 訂單評論 | 1～5 星、文字、最多 5 張圖片、修改評論 |
| 公開評論 | 帳號遮罩、訂單商品摘要、圖片、搜尋與後端分頁 |
| 圖片儲存 | 商品與評論圖片以二進位內容保存於 MySQL |
| 商品報表 | 使用 JasperReports 產生商品 PDF 清單 |

付款功能保存付款方式與狀態，建立訂單時為待付款，不代表已進行銀行扣款。超商取貨使用門市名稱與代碼表單，不會因此自動向物流業者建立配送委託。

## 2. 技術架構與目錄

### 技術組成

| 層級 | 技術 |
| --- | --- |
| 前端 | React 19、JavaScript、CSS |
| 開發伺服器 | Vite 與 API 代理 |
| 後端 | Java 21、Spring Boot 4.1.1、Maven |
| 資料存取 | Spring Data JPA、MyBatis |
| 資料庫 | MySQL 8.0，Docker 提供資料庫執行環境 |
| 驗證 | BCrypt 密碼雜湊、RSA 簽章 JWT、Refresh Token |
| 報表 | JasperReports |

### 專案目錄

```text
專案根目錄/
├─ bakeryweb/
│  ├─ vite.config.js           API 代理設定
│  ├─ public/                  Logo 等靜態資源
│  └─ src/
│     ├─ App.jsx               頁面、登入狀態與購物車
│     ├─ api/ApiService.js     HTTP 請求與 Token 管理
│     └─ components/
│        ├─ Navbar.jsx         導覽列
│        ├─ Login.jsx          登入
│        ├─ Register.jsx       註冊
│        ├─ Products.jsx       商品列表
│        ├─ Cart.jsx           購物車
│        ├─ Checkout.jsx       結帳表單
│        ├─ Orders.jsx         訂單與明細
│        ├─ OrderReviewForm.jsx 評論新增與修改
│        └─ Reviews.jsx        公開評論與搜尋
└─ bakeryusercart/
   ├─ pom.xml
   └─ src/main/
      ├─ java/demo/usercart/
      │  ├─ controller/        API 入口
      │  ├─ service/           業務介面與實作、驗證及交易
      │  ├─ dao/               資料存取介面
      │  ├─ daojpaimpl/        JPA DAO 實作
      │  ├─ daomybatisimpl/    MyBatis DAO 實作
      │  ├─ repository/       JPA 查詢
      │  ├─ mapper/           MyBatis Mapper 介面
      │  ├─ model/            Entity、列舉與目前的 Token 工具
      │  ├─ dto/              請求與回應格式
      │  ├─ exception/        業務例外與錯誤回應
      │  └─ runner/           商品圖片匯入入口
      └─ resources/
         ├─ application.properties
         ├─ Mapper/           SQL XML
         ├─ keys/             JWT 金鑰
         ├─ reports/          報表範本
         └─ fonts/            報表字型
```

### 分層責任

```text
React → ApiService → Controller → Service → DAO 介面
                                             ├─ JPA 實作 → Repository
                                             └─ MyBatis 實作 → Mapper XML
                                                                 ↓
                                                               MySQL
```

- **Controller**：接收 JSON、multipart、參數及 Header，解析登入身分並回傳 HTTP 結果。
- **Service**：集中業務規則，例如評論歸屬、配送付款限制、價格計算及交易範圍。
- **DAO**：定義資料存取契約，Service 以 `@Qualifier` 選擇實際注入的實作。
- **Repository／Mapper**：執行查詢與寫入。
- **DTO**：控制請求及回應欄位，避免將前端輸入直接當作完整資料庫實體保存。

現有訂單使用 MyBatis DAO，評論與 Refresh Token 使用 JPA DAO。資料存取實作可替換的範圍依各 DAO 已提供的方法與 Bean 而定。商品 PDF 入口直接透過 ProductDao 查詢報表資料。

## 3. 前後端通訊與設定

`ApiService.js` 使用相對路徑 `/api`，`vite.config.js` 將請求代理到後端：

```text
瀏覽器頁面 localhost:5173
      ↓ fetch('/api/products')
瀏覽器請求 localhost:5173/api/products
      ↓ Vite 代理
Spring Boot localhost:8080/api/products
      ↓
資料庫查詢 → 回傳商品 JSON
```

瀏覽器這一段與頁面同來源，8080 是 Vite 轉送的目的地。CORS 允許來源設定不會改變 fetch 的網址，也不能取代 JWT 權限檢查。

| 位置 | 設定責任 |
| --- | --- |
| `vite.config.js` | 開發環境 API 代理 |
| `ApiService.js` | API 路徑、Header、Token 刷新與重試 |
| `application.properties` | 資料庫、JPA／MyBatis、Cookie、上傳及圖片匯入 |
| `JwtUtility.java` | 金鑰讀取、JWT 產生與驗證 |
| `UserController.java` | Refresh Token Cookie 設定及清除 |

Docker 執行 MySQL，Volume 保存資料；Workbench 只是管理用戶端。關閉 Workbench 不會停止 MySQL，停止資料庫容器則會影響資料查詢。Vite 的開發代理與正式部署的反向代理是不同設定層次。

## 4. 網站操作流程

```text
瀏覽商品 → 加入購物車 → 確認數量 → 登入 → 選擇配送與付款
                                                  ↓
公開評論 ← 新增／修改評論 ← 查看訂單明細 ← 建立訂單
```

1. 選擇商品分類、數量並加入購物車。
2. 確認購物車；刪除或數量減至零時先確認移除。
3. 登入後填寫收件人、手機與配送專屬資料。
4. 選擇付款方式，確認預估金額後送出。
5. 後端驗證及建立訂單，前端依回傳結果顯示金額、清空購物車並切換至訂單。
6. 在本人訂單明細評分、撰寫評論及上傳圖片。
7. 公開評論頁提供閱讀、關鍵字搜尋及頁次切換。

## 5. 會員登入與權限機制

### 註冊與登入

`UserServiceImpl.register()` 檢查帳密與重複帳號、Email，以 BCrypt 雜湊密碼後保存。登入的 `login()` 查詢會員，以 `passwordEncoder.matches()` 比對密碼；不是先解密資料庫密碼。

成功後產生 Access Token，並呼叫 `RefreshTokenServiceImpl.createToken()` 建立刷新憑證，由 Controller 回傳 JSON 與 Cookie。

| 資料 | 保存位置 | 用途 |
| --- | --- | --- |
| 密碼雜湊 | users | 驗證密碼 |
| Access Token | ApiService.js 記憶體 | 15 分鐘效期，用於受保護 API |
| Refresh Token 原始值 | HttpOnly Cookie | 取得新憑證，初始期限 7 天 |
| Refresh Token SHA-256 | refresh_tokens.token_hash | 查核刷新憑證，不保存原始 Token |

Cookie 使用 `/api/user` 路徑、SameSite Strict，Secure 依環境設定。Access Token 透過 Authorization 傳遞，不存放於 Local Storage。

### 恢復登入與請求重試

`App.jsx` 載入時呼叫 `refreshLogin()`。重新整理會失去記憶體 Token，因此使用 Cookie 恢復登入，成功後重新建立前端狀態。

`authFetch()` 先等待正在進行的刷新；沒有 Token 時先刷新，再以 Bearer Header 發送請求。遇到 401 時檢查是否已有新 Token，必要時換發，原請求最多重試一次。

同一頁面的請求共用 `refreshPromise`，避免每筆 API 都同時刷新；它不是跨瀏覽器分頁的全域鎖。

### Refresh Token 輪替

`refresh()` 在交易內：

1. 驗證格式，計算 SHA-256。
2. 依雜湊查詢並鎖定紀錄。
3. 檢查已使用、撤銷與到期狀態。
4. 標記舊憑證已使用，建立新憑證。
5. 沿用同一 `familyId` 與原始到期時間，不因刷新持續延長七天。
6. 回傳新 Access Token，由 Controller 更新 Cookie。

已使用的 Token 再次出現時撤銷同一家族。此分支回傳失敗狀態，使撤銷能完成交易，不拋出會使撤銷本身回滾的例外。

### 登出與歸屬檢查

`logout()` 撤銷登入家族，Controller 清除 Cookie，前端清除 Token 及購物車。已發出的 Access Token 沒有因此加入即時黑名單，仍有原先效期。

需要登入的 Controller 呼叫 `extractUsernameFromAuthorization()`，再檢查訂單或評論歸屬。前端顯示登入按鈕或隱藏功能只影響操作介面；後端才決定是否能讀寫資料。公開商品及評論可直接讀取，一般會員不能查詢全部會員訂單。

## 6. 商品與購物車邏輯

### 商品列表

`Products.jsx` 依分類呼叫 `fetchProducts()` 或 `fetchProductsByCategory()`，保存結果後用 `slice()` 顯示每頁 12 筆。分類變更時回到第一頁，各商品選購數量依商品 ID 分別保存。

這是前端分頁：先取回該分類商品，再切出目前要顯示的資料。

### 購物車

`App.jsx` 管理 cart state，透過 props 傳遞至各元件。

| 方法 | 邏輯 |
| --- | --- |
| `addToCart()` | 新商品加入，已有商品合併數量 |
| `updateCartQuantity()` | 調整指定商品數量 |
| `removeFromCart()` | 移除指定項目 |
| `handleGoToCheckout()` | 檢查登入及購物車後切換頁面 |
| `handleCheckoutSuccess()` | 依後端訂單顯示結果、清空購物車及切換頁面 |

移除前先取得確認，再更新 state。`Cart.jsx` 以單價乘數量累加小計。

購物車是 React 記憶體資料，不是 Redis 或持久化資料庫購物車；重新整理後可能清空。畫面金額提供預覽，成交金額由後端決定。

## 7. 結帳與訂單邏輯

### 配送及付款規則

| 配送方式 | 運費 | 專屬資料 | 付款方式 |
| --- | ---: | --- | --- |
| 黑貓宅配 | NT$140 | 配送地址 | 銀行轉帳 |
| 7-11 取貨 | NT$60 | 門市名稱、代碼 | 轉帳、貨到付款 |
| 全家取貨 | NT$60 | 門市名稱、代碼 | 轉帳、貨到付款 |
| 到店取貨 | NT$0 | 取貨日期 | 轉帳、到店現金 |

`DeliveryMethod` 定義配送方式與運費，`PaymentMethod.supports()` 驗證付款組合。使用固定列舉代碼，避免自由文字導致判斷不一致。

### 建立訂單的方法

`submitOrder()` 只送商品 ID、數量、收件及配送付款資料，不傳成交價格、總額、會員 ID 或已付款狀態。

`OrderController.createOrder()` 接收 `CheckoutRequest`，由 JWT 取得帳號，呼叫 `OrderServiceImpl.checkout()`，成功回傳 HTTP 201。

`checkout()` 依序執行：

1. 檢查會員與非空購物車。
2. 驗證配送、付款及收件人；手機為 09 開頭的十碼數字。
3. 宅配保存地址、超商保存門市資訊、到店保存取貨日期；取貨日期不得早於台北時區的今天。
4. 檢查商品 ID、正數數量與重複商品列，逐筆查詢資料庫。
5. 驗證價格為有限、非負且可保存的整數元，使用 BigDecimal 計算。
6. 由後端取得運費，設定商品小計、總額及待付款狀態。
7. 保存主檔及全部明細，金額轉整數欄位時檢查範圍。

因此使用者即使修改前端金額，也不能直接指定後端成交價。

### 歷史價格快照

建立明細時保存商品名稱、單價及數量：

```java
orderItem.setProductTitle(product.getTitle());
orderItem.setProductPrice(unitPrice.intValueExact());
orderItem.setQuantity(item.quantity());
```

`products.price` 是目前售價；`orderitems.productPrice` 是成交單價；`orders.totalPrice` 是訂單總額。歷史查詢直接讀訂單，不用商品現價重算。

例如以 100 元購買兩件商品，之後現價改成 120 元，舊訂單仍保留 100 元單價及原總額。

### 訂單交易

`checkout()` 使用 `@Transactional(rollbackFor = Exception.class)`。MyBatis DAO 先新增訂單並取得自動 ID，再將 ID 關聯至各明細後保存。交易失敗時回滾，避免只保存訂單主檔或部分商品。

`Orders.jsx` 顯示保存的金額、商品明細及配送付款資料，後端同時驗證資料屬於登入者。

## 8. 評論圖片與交易

### 新增評論

`OrderReviewForm.jsx` 收集評分、文字及圖片，`buildReviewFormData()` 將 JSON 放入 review 部分、檔案放入 images 部分。

Controller 使用 `@RequestPart`，因為請求同時包含 JSON 與二進位檔案。前端交由瀏覽器設定 multipart boundary，不手動拼接 Content-Type。

`createReview()` 驗證訂單歸屬、重複評論、評分、文字及圖片後，保存評論與圖片。一張訂單對應一筆評論。

| 規則 | 限制 |
| --- | --- |
| 評分 | 必填，1～5 星 |
| 文字 | 可空白，最多 2,000 個 Unicode 字元 |
| 圖片 | 可不附圖，最多 5 張 |
| 單檔 | 最多 5 × 1024 × 1024 bytes |
| 格式 | JPEG、PNG，驗證實際內容 |
| 尺寸 | 單邊最多 10,000 像素，總像素最多 20,000,000 |
| 整個請求 | multipart 上限 30MB |

`ReviewImageValidator` 驗證大小、格式與解碼結果，不只看副檔名。圖片本體與 MIME 存入資料庫，列表以 URL 指向獨立圖片 API。

### 修改評論

`updateReview()` 接收 version、評分、文字、retainedImageIds 與新圖片：

1. 檢查評論存在、本人歸屬及版本。
2. 檢查保留圖片 ID 無重複，且都屬於這筆評論。
3. 計算保留加新增的總數並驗證新圖片。
4. 更新評論、刪除不保留的圖片。
5. 將保留圖片暫移到負數排序，再排回 0、1、2……，避免排序唯一限制衝突。
6. 新圖片接續排列，全部成功後提交。

`@Version` 在更新時檢查樂觀鎖，避免較舊表單覆蓋別人的更新。`flush()` 將 SQL 送到資料庫以檢查限制，**不等於 commit**；中間排序仍可隨交易回滾。

### 交易及錯誤處理

新增和修改使用 `@Transactional(rollbackFor = Exception.class)`，圖片與評論在同一資料庫交易，失敗時不保留部分更新。

寫入前驗證失敗屬於拒絕寫入；執行 SQL 後出現符合回滾條件的例外，才是撤銷已執行的交易修改。

`GlobalExceptionHandler` 將評論驗證、權限、不存在、版本／完整性衝突及上傳過大轉為 400、403、404、409、413，前端讀取 message 顯示。

## 9. 評論搜尋與後端分頁

`Reviews.jsx` 區分搜尋框輸入與已送出的查詢條件，每次傳入 keyword、page、size，目前畫面使用每頁 10 筆。

`getReviews()` 的流程：

- Controller 接收參數，Service 驗證 page 不小於 0、size 為 1～50。
- 建立 PageRequest，交由資料庫取得指定頁面。
- 無關鍵字時使用 `findAllByOrderByCreatedAtDescIdDesc()` 對應的 DAO 查詢。
- 有關鍵字時，將 LIKE 的特殊符號跳脫，匹配評論文字或訂單商品名稱。
- 商品匹配使用 EXISTS，避免多件商品符合時重複列出評論。
- 以 createdAt、id 降冪排序，使用相同條件計算總筆數。

回應包含 content、page、size、totalElements、totalPages。API 頁碼從 0 起算，前端依總頁數產生按鈕；只有一頁時無需顯示多頁選擇。

`toResponse()` 組合公開 DTO、商品摘要及圖片 URL。`maskUsername()` 將三字元以上帳號顯示為首字元加 *** 加尾字元；兩字元保留首字元，一字元只顯示 *。這只改變公開顯示，不改寫 users 的原帳號。

## 10. 商品圖片與報表

### 商品圖片

| 欄位 | 用途 |
| --- | --- |
| image | 原始圖片檔名，供匯入比對 |
| image_content_type | MIME 類型 |
| image_data | 圖片二進位內容 |

`ProductImageImportRunner` 依啟用設定呼叫 `importProductImages()`，讀取指定資料夾、驗證內容，再透過 `saveImageIfAbsent()` 補入缺少圖片的商品。匯入有交易保護，不任意覆蓋既有圖片。

`getProductImage()` 讀取資料庫內容，Controller 設定 Content-Type、長度及 nosniff 回傳。商品及購物車以 `/api/products/{productId}/image` 載入照片。

BLOB 保存後不依賴原始照片檔案才能存在；Logo、報表資源仍各自管理。資料庫備份需包含圖片內容，Volume 持久化不是另一份備份。

### 商品 PDF

`ProductReportController.downloadProductReport()` 查詢商品，建立 JRBeanCollectionDataSource，讀取並編譯 JRXML，填入資料後輸出 PDF。

回應使用 application/pdf 與 inline Content-Disposition。報表呈現查詢時的商品資料，與歷史訂單的價格快照用途不同。

## 11. 資料表與 API

### 主要資料表

| 資料表 | 用途 |
| --- | --- |
| users | 會員及密碼雜湊 |
| products | 商品現價、分類與圖片 |
| orders | 訂單主檔、金額、配送與付款 |
| orderitems | 商品名稱、成交單價與數量快照 |
| refresh_tokens | Token 雜湊、家族、效期與使用／撤銷狀態 |
| order_reviews | 評分、評論、時間及版本 |
| order_review_images | 圖片本體、MIME、大小與排序 |

```text
users ──< orders ──< orderitems
  │          │
  │          └── order_reviews ──< order_review_images
  └──< refresh_tokens
```

一位會員可有多張訂單；一張訂單有多筆明細，最多一筆評論；每筆評論可有多張圖片。

### API 概覽

| 方法 | 路徑 | 用途 |
| --- | --- | --- |
| POST | /api/user/register | 註冊 |
| POST | /api/user/login | 帳密登入 |
| POST | /api/user/refresh | 使用 Cookie 換發憑證 |
| POST | /api/user/logout | 登出 |
| GET | /api/products | 公開商品列表 |
| GET | /api/products/category/{category} | 分類查詢 |
| GET | /api/products/productid/{productid} | 單品查詢 |
| GET | /api/products/{productId}/image | 商品圖片 |
| GET | /api/products/report | 商品 PDF |
| POST | /api/orders | JWT 驗證後結帳，成功 201 |
| GET | /api/orders/{username} | 本人訂單 |
| GET | /api/orders/orderid/{orderid} | 本人單筆訂單 |
| GET | /api/items/{orderId} | 本人訂單明細 |
| GET | /api/reviews | 公開評論搜尋及分頁 |
| GET | /api/reviews/order/{orderId} | 本人訂單評論，未評論回傳 204 |
| POST | /api/reviews | 新增評論，multipart |
| PUT | /api/reviews/{reviewId} | 修改本人評論，multipart |
| GET | /api/reviews/{reviewId}/images/{imageId} | 公開評論圖片 |

## 12. 核心設計整理

| 設計 | 解決的問題 |
| --- | --- |
| Service 業務與 DAO 存取分離 | 避免不同資料存取方式重複實作業務規則 |
| 後端核價及配送列舉 | 避免前端指定不合法金額與付款組合 |
| 訂單快照 | 商品改價後保留原成交內容 |
| JWT 與 Refresh Token 輪替 | 短效憑證搭配可撤銷的長效登入紀錄 |
| 本人歸屬檢查 | 防止透過修改 ID 操作別人的資料 |
| 評論圖片同一交易 | 避免部分圖片或部分修改被保存 |
| 樂觀鎖版本 | 防止過時表單覆蓋新資料 |
| DTO 與帳號遮罩 | 控制公開資訊 |
| 後端分頁、EXISTS 搜尋 | 取得指定頁面且避免重複評論 |
| 獨立圖片 API | 圖片依需要另外讀取 |

整體流程以「前端協助輸入，後端驗證規則，資料庫保存交易結果」為核心。畫面驗證改善操作體驗，後端權限及交易決定最終能保存的結果。

