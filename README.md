# 潘媽媽烘焙坊購物車專案：專案介紹與使用方法

本專案是一個前後端分離的烘焙商品購物網站，提供商品瀏覽、會員登入、購物車、結帳、訂單查詢及訂單評論功能。前端負責畫面與操作，後端處理權限、資料驗證與交易，MySQL 保存會員、商品、訂單及圖片等資料。

## 目錄

1. [專案功能](#1-專案功能)
2. [技術架構與目錄](#2-技術架構與目錄)
3. [執行環境](#3-執行環境)
4. [資料庫設定](#4-資料庫設定)
5. [後端啟動](#5-後端啟動)
6. [前端啟動](#6-前端啟動)
7. [網站操作流程](#7-網站操作流程)
8. [登入與權限機制](#8-登入與權限機制)
9. [評論圖片與交易](#9-評論圖片與交易)
10. [商品資料及圖片維護](#10-商品資料及圖片維護)
11. [資料表與-api](#11-資料表與-api)
12. [日常啟停與問題排查](#12-日常啟停與問題排查)
13. [目前版本需確認的事項](#13-目前版本需確認的事項)

## 1. 專案功能

### 商品與購物車

- 商品列表顯示商品名稱、圖片與價格。
- 可依麵包、吐司、點心等分類瀏覽商品。
- 商品頁提供分頁與加入購物車操作。
- 購物車可調整數量、計算商品小計及移除商品。
- 數量即將減至零或按刪除時，先確認是否移除商品。
- 提供商品 PDF 清單功能，報表由後端產生。

### 會員與訂單

- 註冊帳號、登入與登出。
- 使用短效 Access Token 與長效 Refresh Token 維持登入。
- 結帳頁提供配送、收件資料與付款方式選擇。
- 查詢自己的訂單、商品明細及配送付款資訊。
- 後端檢查訂單歸屬，避免會員查詢別人的訂單。

### 訂單評論

- 對自己的訂單評分及撰寫評論。
- 支援圖片上傳、預覽及修改評論。
- 公開列表顯示評分、內容、圖片與該訂單購買的商品。
- 公開帳號顯示經過遮罩處理。
- 支援評論文字或訂單商品名稱的關鍵字搜尋與後端分頁。

## 2. 技術架構與目錄

| 項目 | 使用技術 |
| --- | --- |
| 前端 | React 19、Vite、JavaScript、CSS |
| 後端 | Java 21、Spring Boot 4.1.1、Maven |
| 資料存取 | Spring Data JPA、MyBatis |
| 資料庫 | MySQL 8.0 |
| 登入驗證 | JWT、RSA 金鑰、Refresh Token |
| 報表 | JasperReports |
| 資料庫執行環境 | Docker 或本機 MySQL |

```text
專案根目錄/
├─ bakeryweb/
│  ├─ package.json             前端依賴與執行指令
│  ├─ package-lock.json        依賴版本鎖定
│  ├─ vite.config.js           開發伺服器與 API 代理
│  ├─ public/                  Logo 等公開靜態檔案
│  └─ src/
│     ├─ App.jsx               頁面切換、登入狀態、購物車
│     ├─ api/ApiService.js     API 呼叫與 Access Token 管理
│     └─ components/          商品、購物車、結帳、訂單與評論元件
└─ bakeryusercart/
   ├─ pom.xml                  Java 依賴與建置設定
   ├─ mvnw.cmd                 Windows Maven Wrapper
   └─ src/main/
      ├─ java/demo/usercart/
      │  ├─ controller/       HTTP API 與請求處理
      │  ├─ service/          業務規則、資料驗證與交易
      │  ├─ dao/              資料存取介面及相關實作
      │  ├─ repository/       JPA Repository
      │  ├─ mapper/           MyBatis Mapper 介面
      │  ├─ model/            Entity、列舉與 Token 工具
      │  ├─ dto/              API 請求與回應格式
      │  ├─ exception/        例外與統一錯誤回應
      │  └─ runner/           商品圖片匯入啟動程式
      └─ resources/
         ├─ application.properties
         ├─ Mapper/           SQL 映射 XML
         ├─ keys/             本機 JWT 金鑰
         ├─ reports/          報表範本
         └─ fonts/            報表字型
```

DAO 實作的實際套件位置以專案檔案為準。主要呼叫方向為：

```text
React 畫面 → ApiService → Controller → Service → DAO
                                               ├─ JPA Repository
                                               └─ MyBatis Mapper → SQL XML
                                                        ↓
                                                      MySQL
```

Service 集中處理業務邏輯，使用 `@Qualifier` 指定要注入的 DAO 實作。不是所有功能都已提供兩種資料存取實作，切換前需確認對應 Bean 存在。

## 3. 執行環境

準備以下工具：

- JDK 21，並確認 IDE 與命令列使用相容的 Java。
- 可執行目前 Vite 版本的 Node.js 與 npm；安裝時若出現 engines 不相容訊息，應先調整 Node.js 版本。
- Docker Desktop（使用容器資料庫時）。
- MySQL Workbench 或其他 MySQL 用戶端。
- Eclipse、IntelliJ IDEA 或其他 Java IDE；前端可使用 VS Code。

常用開發連接埠：

| 服務 | 位址／連接埠 | 說明 |
| --- | --- | --- |
| 前端 | `http://localhost:5173` | Vite 預設開發位址，以啟動輸出為準 |
| 後端 | `http://localhost:8080` | Spring Boot API |
| 原本本機 MySQL | `localhost:3306` | 目前 properties 仍是此設定 |
| 既有 Docker MySQL | `127.0.0.1:3307` | 電腦端對外連接埠 |
| Docker 內 MySQL | `3306` | 容器內部連接埠 |

## 4. 資料庫設定

### 4.1 啟動既有容器

若先前已建立名為 `bakery-mysql` 的容器，開啟 Docker Desktop，啟動該容器；或在終端機執行：

```bat
docker start bakery-mysql
docker ps
```

以上命令不會建立新容器。第一次安裝的環境需先建立 MySQL、資料庫、帳號與持久化 Volume。

### 4.2 Workbench 連線

既有 Docker 環境使用：

| 欄位 | 範例 |
| --- | --- |
| Hostname | `127.0.0.1` |
| Port | `3307` |
| Username | `bakery_user` |
| Password | 建立容器時設定的密碼 |
| Schema | `mydb` |

可執行以下 SQL 確認資料：

```sql
USE mydb;
SHOW TABLES;
SELECT COUNT(*) AS product_count FROM products;
```

空白資料庫可以由既有備份還原結構與資料。Hibernate 的 `ddl-auto=update` 不會自動建立完整商品內容，也不能代替備份或正式資料庫版本遷移。

### 4.3 後端連線設定

開啟 `bakeryusercart/src/main/resources/application.properties`。

若後端在 Windows 上執行，要連到上述 Docker MySQL，可設定：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3307/mydb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

`DB_USERNAME` 與 `DB_PASSWORD` 是建議採用的環境變數寫法，需自行修改 properties 才會生效；目前原始檔仍是直接設定值。可在 IDE 的 Run Configuration → Environment 設定兩個變數。

## 5. 後端啟動

### 5.1 匯入專案

以 Existing Maven Project 匯入 `bakeryusercart`，等待 Maven 下載依賴。使用 JDK 21，確認 IDE 能正確處理 Lombok。

### 5.2 JWT 金鑰

目前 `JwtUtility.java` 從 classpath 讀取：

```text
src/main/resources/keys/private.pem
src/main/resources/keys/public.pem
```

私鑰須為程式支援的 PKCS#8 PEM，公鑰須為 X.509 PEM，且為同一組 RSA 金鑰。不要公開或共用正式私鑰。

新環境沒有金鑰時，可在有 OpenSSL 的環境中，於自己準備的空白金鑰目錄執行下列指令，再放到上述位置。不要覆蓋正在使用的金鑰：

```text
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem
```

更換金鑰後，舊 Access Token 將無法通過新公鑰驗證。

### 5.3 開發設定

```properties
app.auth.cookie-secure=false
app.product-image.import-enabled=false
```

第一個設定適用於本機 HTTP。使用正式 HTTPS 時應調整 Cookie 設定；第二個設定代表平常啟動不執行商品圖片匯入。

### 5.4 執行

在 IDE 啟動 Spring Boot 主程式；或開啟 CMD，切換到 `bakeryusercart` 後執行：

```bat
mvnw.cmd spring-boot:run
```

確認 Console 顯示應用程式啟動完成，且沒有資料庫、金鑰或 Mapper 初始化錯誤。可開啟 `http://localhost:8080/api/products` 檢查是否回傳商品 JSON。

## 6. 前端啟動

在另一個终端機切換至 `bakeryweb`，執行：

```bat
npm ci
npm run dev
```

`npm ci` 依 package-lock.json 安裝依賴；如果 lockfile 與 package.json 不一致，先確認哪份檔案才是正確版本。依終端機輸出的 Local URL 開啟網站。

目前 `vite.config.js` 將 `/api` 代理到：

```text
http://localhost:8080
```

因此瀏覽器請求 `/api/products` 時，Vite 會轉送給 Spring Boot。後端連接埠變更時，也需同步修改代理。

其他指令：

| 指令 | 用途 |
| --- | --- |
| `npm run build` | 建置前端，輸出至 dist |
| `npm run lint` | 執行 ESLint |
| `npm run preview` | 預覽前端建置結果 |

開發代理設定不能當作正式網站的 API 路由配置。正式服務需另外設定 `/api` 反向代理或前端 API 位址。

## 7. 網站操作流程

### 7.1 註冊與登入

1. 開啟「帳戶登入」。
2. 初次使用者前往註冊，填寫帳號、密碼、姓名及 Email。
3. 註冊成功後登入。
4. 導覽列顯示帳號與登出按鈕。

登入失敗時依畫面訊息檢查帳密。重新開啟網站仍保持登入，可能是有效的 Refresh Token 恢復登入，屬於目前設計。

### 7.2 瀏覽商品

1. 點選「商品列表」。
2. 選擇分類與頁次。
3. 選擇數量並加入購物車。
4. 導覽列的購物車數量會更新。

商品清單目前由前端顯示分頁；公開評論則由後端分頁，兩者方式不同。

### 7.3 購物車

1. 點選「購物車」。
2. 檢查商品、單價、數量及小計。
3. 使用加減按鈕調整數量。
4. 數量減至零或按刪除時，確認「確定要移除該商品」；取消則保留商品。
5. 登入後前往結帳。

購物車目前由 App.jsx 的 React state 保存。它不是 Redis，也不是持久化的資料庫購物車；重新整理可能清空購物車。

### 7.4 配送與付款

結帳頁填寫收件人姓名、手機，再選擇配送方式：

| 配送方式 | 運費 | 配送專屬資料 | 可選付款方式 |
| --- | ---: | --- | --- |
| 黑貓宅配 | NT$140 | 配送地址 | 銀行轉帳 |
| 7-11 取貨 | NT$60 | 門市名稱、門市代碼 | 貨到付款、銀行轉帳 |
| 全家取貨 | NT$60 | 門市名稱、門市代碼 | 貨到付款、銀行轉帳 |
| 到店取貨 | NT$0 | 取貨日期 | 到店現金付款、銀行轉帳 |

目前超商資料由表單填寫，不代表已串接超商地圖或物流服務。

確認商品小計、運費及總額後送出。例如商品小計 NT$1,000、宅配 NT$140，預計合計 NT$1,140。


目前轉帳銀行、戶名及帳號仍為「待設定」。訂單送出不等於付款完成，本專案沒有在此流程中進行銀行扣款。

### 7.5 查詢訂單

登入後點「訂單」，查看訂單紀錄並展開明細。新訂單預期顯示：

- 訂單編號、建立時間與總額。
- 購買商品、成交單價與數量。
- 商品小計、運費。
- 配送方式、收件人與手機。
- 宅配地址、超商資訊或取貨日期。
- 付款方式與付款狀態。

歷史訂單可能沒有新增的配送欄位，不能將空值視為已填寫的收件資料。

### 7.6 新增及修改評論

1. 開啟自己的訂單明細。
2. 在評論區點選 1 至 5 顆星。
3. 可填寫評論文字，最多 2,000 個 Unicode 字元。
4. 可選擇 JPEG 或 PNG 圖片，確認預覽後送出。
5. 已有評論時可修改評分、文字，新增圖片或移除既有圖片。
6. 按送出修改，成功後才完成資料庫更新。

每張訂單對應一筆評論。修改時既有圖片與新增圖片合計不得超過上限。若出現版本衝突，重新載入最新評論後再修改。

### 7.7 瀏覽公開評論

點導覽列「評論」即可閱讀公開內容。輸入關鍵字搜尋評論文字或該訂單商品名稱，再切換頁次。

帳號遮罩例子：`peter` 顯示 `p***r`；一字元帳號顯示 `*`，二字元帳號顯示首字元加 `*`。這是顯示遮罩，不能保證無法透過評論內容辨識作者。

## 8. 登入與權限機制

| 項目 | 存放位置／行為 |
| --- | --- |
| Access Token | ApiService.js 記憶體變數，有效期 15 分鐘 |
| Refresh Token 原始值 | HttpOnly Cookie，初始期限 7 天 |
| Refresh Token 雜湊 | refresh_tokens 資料表 |
| 受保護 API | Authorization: Bearer 加上 Access Token |

登入取得 Access Token；重新整理後透過 Cookie 呼叫 `/api/user/refresh` 恢復。換發 Refresh Token 時進行輪替，保留該次登入原始到期時間。登出會撤銷對應的 Refresh Token 家族並清除 Cookie。

HttpOnly Cookie 不由前端 JavaScript 直接讀取。Local Storage 沒有 token 是目前設計；App.jsx 還會移除舊版遺留的 token。

需要登入的行為由後端 Controller 驗證 JWT，再由 Controller / Service 檢查資料歸屬。前端隱藏按鈕不能取代後端權限驗證。

商品與公開評論可公開讀取；個人訂單、建立訂單、自己的評論讀取及寫入需要登入。現有查詢全部會員訂單的 API 被禁止，帳號叫 admin 不代表有管理員權限。

## 9. 評論圖片與交易

主要程式職責：

| 檔案 | 工作 |
| --- | --- |
| OrderReviewForm.jsx | 星星、文字、圖片預覽與編輯表單 |
| ApiService.js | 組合 multipart FormData，送出 JSON review 與 images |
| OrderReviewController.java | 以 @RequestPart 接收內容、驗證登入 |
| OrderReviewServiceImpl.java | 歸屬與內容檢查、建立或修改評論、管理交易 |
| ReviewImageValidator.java | 實際圖片格式、大小、尺寸與像素驗證 |
| OrderReview / OrderReviewImage | 保存評論及圖片資料 |
| GlobalExceptionHandler.java | 轉成前端可讀的錯誤回應 |

Service 的寫入方法使用 `@Transactional(rollbackFor = Exception.class)`。評論與圖片的資料庫操作放在同一交易內，正常完成才提交；交易內符合回滾條件的例外會撤銷該交易已執行的資料庫修改。

格式錯誤也可能在寫入前就被驗證攔截，這種情況是「沒有寫入」，不能單靠它證明已寫入的資料確實回滾。

目前評論上傳限制：

| 項目 | 限制 |
| --- | --- |
| 圖片數量 | 最多 5 張 |
| 單張檔案 | 最多 5 MiB（5 × 1024 × 1024 bytes） |
| 格式 | JPEG、PNG |
| 單邊尺寸 | 最大 10,000 像素 |
| 總像素 | 最大 20,000,000 |
| 整個 multipart 請求 | 30 MB（依 Spring 設定解析） |

圖片內容存入資料庫 BLOB。資料庫交易不會自動回滾外部檔案系統的操作；本功能將評論圖片本體放在資料庫，才可與評論一起管理。

## 10. 商品資料及圖片維護

商品圖片欄位：

| 欄位 | 用途 |
| --- | --- |
| image | 保留的原始圖片檔名，供匯入使用 |
| image_content_type | MIME 類型，例如 image/jpeg |
| image_data | 圖片的實際二進位內容 |

圖片已存入 image_data 後，不依賴前端來源檔案才能存在。Logo 等靜態資源與報表圖片來源可能仍使用檔案，不應直接刪除整個 public/images。

維護新商品時：

1. 使用 Workbench 連到網站實際使用的資料庫。
2. 依 Product 實體與資料表填寫未使用的 id、名稱、分類、價格及其他欄位。
3. 如果使用現有圖片匯入流程，image 欄位填對應檔名，將來源圖片放到設定的目錄。
4. 設定 `app.product-image.import-directory` 為實際絕對路徑。
5. 暫時設 `app.product-image.import-enabled=true`，重新啟動後端。
6. 查看啟動日誌與資料庫，成功後改回 false。

現有匯入流程以補入未有圖片的商品為主，不能當作任意覆寫既有圖片的工具。匯入失敗時先查看格式、大小與尺寸錯誤，再修正来源檔案。

可用以下查詢確認是否有圖片內容：

```sql
SELECT id, title, image_content_type,
       OCTET_LENGTH(image_data) AS image_bytes
FROM products
ORDER BY id;
```

資料庫備份必須包含 BLOB 資料。Docker Volume 可持久保存資料，但不等於另外一份備份。

## 11. 資料表與 API

### 11.1 主要資料表

| 資料表 | 用途 |
| --- | --- |
| users | 會員資料與密碼雜湊 |
| products | 商品資訊與商品圖片 |
| orders | 訂單主檔、金額與配送付款資料 |
| orderitems | 訂單商品明細 |
| refresh_tokens | Refresh Token 雜湊、效期與撤銷狀態 |
| order_reviews | 訂單評論、評分與版本 |
| order_review_images | 評論圖片內容與排序 |

密碼雜湊與 token_hash 用途不同：前者驗證密碼，後者驗證刷新憑證。

### 11.2 常用 API

| 方法 | 路徑 | 用途 |
| --- | --- | --- |
| POST | /api/user/register | 註冊 |
| POST | /api/user/login | 登入 |
| POST | /api/user/refresh | 使用 Cookie 換發 Token |
| POST | /api/user/logout | 登出 |
| GET | /api/products | 商品列表 |
| GET | /api/products/category/{category} | 分類商品 |
| GET | /api/products/productid/{productid} | 單一商品 |
| GET | /api/products/{productId}/image | 商品圖片 |
| GET | /api/products/report | 商品 PDF |
| POST | /api/orders | 建立訂單，目前需確認新結帳介面接線 |
| GET | /api/orders/{username} | 個人訂單 |
| GET | /api/orders/orderid/{orderid} | 自己的單筆訂單 |
| GET | /api/items/{orderId} | 訂單明細 |
| GET | /api/reviews?keyword=&page=0&size=10 | 公開評論與搜尋 |
| GET | /api/reviews/order/{orderId} | 自己訂單的評論 |
| POST | /api/reviews | 新增評論，multipart |
| PUT | /api/reviews/{reviewId} | 修改評論，multipart |
| GET | /api/reviews/{reviewId}/images/{imageId} | 評論圖片 |

評論分頁頁碼從 0 起算。列表包含 content、page、size、totalElements、totalPages；預設 size 為 10，前端可傳入自己的每頁筆數。

## 12. 日常啟停與問題排查

啟動順序：MySQL → Spring Boot → Vite → 瀏覽器。停止時可先關閉前端與後端，再停止資料庫容器。

| 現象 | 優先檢查 |
| --- | --- |
| 商品讀取失敗 | 後端、MySQL 是否啟動，以及 API 代理 |
| MySQL 連線失敗 | 3306 / 3307 是否用錯、帳密、資料庫名稱 |
| 網頁白畫面 | Chrome Console 是否有 React 變數或元件錯誤 |
| refresh 回傳 401 | 尚未登入、Cookie 過期或已撤銷 |
| 登入後仍一直 401 | Cookie、主機名是否一致、JWT 金鑰與效期 |
| 訂單不存在或無法查看 | 訂單編號、登入帳號及訂單歸屬 |
| 成功提示金額為 0 | 前後端 DTO 與 OrderController 是否仍走舊流程 |
| 上傳 413 | 單檔與整個 multipart 大小限制 |
| 圖片驗證失败 | 真實格式、尺寸、像素或損毀圖片 |
| 修改評論 409 | 重新取得最新版本後修改 |
| 商品圖片顯示不出來 | image_data、MIME 及圖片 API 回應 |
| Linux 找不到 Mapper SQL | Mapper 目錄大小寫與 mapper-locations |

停掉 Docker MySQL 後，前端可能仍可顯示已載入的畫面，但需要資料庫的 API 會失敗。Workbench 是管理用戶端，關閉 Workbench 不等於關閉 MySQL。
