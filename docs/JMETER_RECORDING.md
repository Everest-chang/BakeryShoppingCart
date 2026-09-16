# JMeter 錄製與重播

## 目的

記錄瀏覽器操作購物網站時產生的 HTTP 請求，再由 JMeter 重播。JMeter 不會重播滑鼠動作，也不會像瀏覽器一樣執行 React。

## 1. 啟動環境

- 啟動 MySQL、Spring Boot 後端及 Vite 前端。
- 確認 `http://localhost:5173` 可以使用。
- 執行 JMeter 安裝目錄內 `bin/jmeter.bat`，保留其命令視窗。

## 2. 建立測試計畫

在 Test Plan 下新增 Thread Group，使用者、Ramp-up 秒數、Loop Count 暫時都設為 1。

建立以下結構：

```text
Test Plan
├─ Thread Group
│  ├─ Recording Controller
│  └─ View Results Tree
└─ HTTP(S) Test Script Recorder
   └─ View Results Tree
```

- Recording Controller：Thread Group 右鍵 → Add → Logic Controller。
- 錄製器：Test Plan 右鍵 → Add → Non-Test Elements。
- View Results Tree：右鍵 → Add → Listener。

錄製器 Port 設為 `8888`，Target Controller 選剛建立的 Recording Controller。

Requests Filtering → URL Patterns to Include → Add：

```regex
.*localhost:5173/.*
```

若只想錄製 API，可改成 `.*localhost:5173/api/.*`。過濾器不會刪除先前已錄到的項目。

## 3. 開始錄製

按錄製器內的 Start，不是上方執行測試的綠色三角形。

另外開啟一個 Windows CMD，貼上整行：

```bat
start "" chrome.exe --user-data-dir="%TEMP%\bakery-jmeter-chrome" --proxy-server="http://127.0.0.1:8888" --proxy-bypass-list="<-loopback>" "http://localhost:5173"
```

這會使用獨立瀏覽器設定，讓 localhost 請求也經過錄製代理。後續操作需在這個視窗進行；若同一個專用視窗已開啟，先關閉再以指令啟動。

本機 HTTP 網站不需安裝 HTTPS 錄製憑證；錄製 HTTPS 時需額外處理憑證信任。

重新整理首頁並點商品列表，確認 Recording Controller 出現 `/api/products` 等請求。錄製是真實操作，送出訂單會真的寫入目前連線的資料庫。

按錄製器 Stop，另存為 `.jmx`。停止代理後關閉專用 Chrome，平常使用原本的瀏覽器。

## 4. 先重播單筆商品查詢

1. 複製錄到的 `/api/products` 請求，貼到 Thread Group 直屬層級。
2. 命名為「錄製重播－商品查詢」。
3. 停用 Recording Controller 及其他請求，只保留這一筆。
4. 確認 GET、http、localhost、5173、路徑 `/api/products`。
5. 使用者、Ramp-up、Loop Count 均設為 1。
6. 按上方綠色三角形執行。
7. 在 Thread Group 的 View Results Tree 確認 HTTP 200 與商品 JSON。

錄製器底下的報告是錄製期間的觀察資料，不應當作重播壓測報告。錄製時看到的其他網站錯誤，也不能直接歸因於購物網站。

## 5. 加入正確性檢查

在商品請求下新增 Response Assertion，選 Response Code、Equals，填入 `200`。之後再依實際 JSON 結構檢查內容；只有回傳 200 不保證業務內容正確。

## 6. 登入、訂單與評論的後續處理

- 用測試帳號登入，從登入回應擷取最新 Access Token。
- 後續 Authorization 使用 `Bearer ${accessToken}`，不要重播錄製時的固定 JWT。
- 使用 HTTP Cookie Manager 管理 Cookie，整理錄到的固定 Cookie 標頭。
- 訂單 ID、評論 ID 需從回應擷取，供後續請求引用。
- 錯誤情境要斷言預期的 400、401、403 等狀態及資料結果。
- 不要把含密碼、Cookie、JWT、個人資料的原始錄製檔直接公開。

## 7. 與自動化的關係

先整理可重複執行的測試腳本，再串入 pipeline。正式負載測試使用非 GUI 模式；CI 還需要檢查結果中的失敗紀錄，不能只看 JMeter 程序是否結束。

目前文件未附 `.jmx`，請將自己整理並移除敏感內容的腳本放入 `tests/jmeter/`。不必上傳整套 JMeter 安裝目錄。

官方參考：https://jmeter.apache.org/usermanual/component_reference.html
