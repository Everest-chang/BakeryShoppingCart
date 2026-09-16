# 潘媽媽烘焙坊購物車系統（bakeryusercart）

## 專案簡介

本專案為 Spring Boot + React
的烘焙坊購物車系統，包含會員、商品、訂單與購物車相關功能，後端採分層架構，並整合
MySQL、JPA、MyBatis 與 JasperReports。

## 主要技術

-   Java / Spring Boot
-   Spring MVC
-   Spring Data JPA
-   MyBatis
-   MySQL
-   React / JavaScript
-   Maven
-   JasperReports
-   JWT（RS256 / RSA 非對稱式簽章）
-   BCrypt

## 後端架構

``` text
Controller
↓
Service interface
↓
ServiceImpl
↓
DAO interface
↓
DAOImpl
↓
Repository / MyBatis Mapper
↓
MySQL
```

### 分層原則

-   Controller：接收前端 HTTP Request，呼叫 Service。
-   Service / ServiceImpl：負責商業邏輯。
-   DAO / DAOImpl：負責選擇及封裝資料存取方式。
-   Repository：使用 Spring Data JPA 存取資料庫。
-   Mapper：使用 MyBatis 搭配 XML SQL 存取資料庫。
-   Model：存放 User、Product、Order、OrderItem 等資料模型。

## 主要套件結構

``` text
demo.usercart
├── controller
├── dao
├── exception
├── mapper
├── model
├── repository
├── service
└── BakeryusercartApplication.java

src/main/resources
├── Mapper/
├── keys/
│   ├── private.pem
│   └── public.pem
├── fonts/
├── reports/
├── static/
├── templates/
├── application.properties
└── jasperreports_extension.properties
```

## 商品功能

商品資料透過 ProductDao 存取，可切換 JPA 或 MyBatis
實作。主要功能包括： - 查詢全部商品 - 依商品 ID 查詢 - 依商品種類查詢 -
商品不存在時透過自訂 Exception 處理錯誤

MyBatis Mapper 主要方法：

``` java
List<Product> findAll();
Product findById(Integer id);
List<Product> findByCategory(String category);
```

## 會員功能

會員系統包含： - 會員註冊 - 使用者名稱重複檢查 - Email 重複檢查 - BCrypt
密碼加密 - 登入密碼驗證 - JWT 驗證相關功能

密碼欄位使用 WRITE_ONLY，讓前端可以傳入密碼，但後端回傳 User JSON
時不輸出密碼。

## JWT 與 RSA 非對稱式簽章

本專案使用 JWT（JSON Web Token）進行會員登入後的身分驗證，並使用 RSA
金鑰組搭配 RS256 演算法進行 JWT 的數位簽章與驗證。

JWT 本身並不是用 RSA 將內容加密，而是利用非對稱式金鑰進行「簽章」：

-   `private.pem`：RSA 私鑰，只由後端持有，用來簽署並產生 JWT。
-   `public.pem`：RSA 公鑰，用來驗證 JWT 簽章是否有效。
-   簽章演算法：`RS256`。
-   JWT 內包含使用者名稱、簽發時間與到期時間。
-   Token 驗證成功後，才允許使用者存取需要登入身分的功能，例如訂單相關
    API。

RSA 金鑰位置：

``` text
src/main/resources/keys/
├── private.pem
└── public.pem
```

JWT 驗證流程：

``` text
會員輸入帳號、密碼
↓
後端驗證會員資料與 BCrypt 密碼
↓
登入成功
↓
使用 RSA Private Key + RS256 簽署 JWT
↓
JWT 回傳給 React 前端
↓
前端保存 JWT
↓
呼叫需要登入的 API 時攜帶 JWT
↓
後端使用 RSA Public Key 驗證 JWT
↓
驗證成功後允許存取
```

相較於原本使用同一組 Secret Key 進行簽章與驗證的 HMAC 對稱式機制， RSA
採用不同的私鑰與公鑰：私鑰負責簽章、公鑰負責驗證，因此驗證端不需要取得私鑰。

> **安全注意事項：** `private.pem` 屬於敏感金鑰，不應提交至公開 Git
> Repository。 實際部署時應透過安全的 Secret / Key 管理方式保存私鑰。

## 訂單功能

Order 與 OrderItem 為一對多關係。

``` text
Order
  ↓ 1:N
OrderItem
```

使用 MyBatis 儲存訂單時，由 ServiceImpl 負責流程：先新增 Order 取得訂單
ID，再逐筆新增 OrderItem。

## React 前端

前端使用 React，主要包含： - Navbar - Footer - Login - Register -
Products - Cart - Orders - ApiService.js

React 透過 `/api` 呼叫 Spring Boot REST API。不同 Response
使用不同讀取方式： - JSON：`res.json()` - 純文字：`res.text()` -
PDF：`res.blob()` 或直接開啟 PDF API

## JasperReports 商品目錄

專案已整合 JasperReports，商品資料可從 MySQL 查詢後產生 PDF 商品目錄。

報表位置：

``` text
src/main/resources/reports/ProductReport.jrxml
```

報表欄位包含： - 商品編號 id - 商品名稱 title - 商品描述 description -
種類 category - 售價 price

中文 PDF 使用 SimHei 字型設定：

``` text
jasperreports_extension.properties
↓
fonts/fonts.xml
↓
fonts/SimHei.ttf
```

網站商品頁最下方提供「商品目錄下載」按鈕，目前按下後以新分頁直接開啟
PDF，而不是強制下載。後端 PDF Response 使用
`Content-Type: application/pdf` 與 `Content-Disposition: inline`。

## 商品 PDF 流程

``` text
React 商品頁
↓
商品目錄下載按鈕
↓
GET /api/products/report
↓
ProductReportController
↓
ProductDao.findAll()
↓
MySQL
↓
JRBeanCollectionDataSource
↓
ProductReport.jrxml
↓
JasperReports
↓
PDF
↓
瀏覽器新分頁顯示
```

## MyBatis 設定

``` properties
mybatis.mapper-locations=classpath:Mapper/*.xml
mybatis.type-aliases-package=demo.usercart.model
mybatis.configuration.map-underscore-to-camel-case=true
```

MyBatis Java Mapper 方法名稱必須與 XML statement 的 `id` 一致，XML
`namespace` 必須對應 Mapper interface 的完整 package 名稱。

## 專案特色

本專案實作前後端分離的購物網站，練習 Spring Boot 分層架構、REST
API、MySQL 資料庫操作、JPA/MyBatis 資料存取、BCrypt 密碼雜湊、 JWT + RSA
RS256 非對稱式簽章驗證、React 前端串接，以及 JasperReports 動態 PDF
報表產生。
