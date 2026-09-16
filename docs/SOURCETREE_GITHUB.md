# 使用 SourceTree 上傳到 GitHub

## 上傳與部署的差別

這裡的流程是將原始碼與 Markdown 文件推送到 GitHub 保存、展示及版本管理。GitHub 會自動顯示根目錄 README.md。

Push 不會讓購物網站自動上線。Spring Boot、MySQL 仍需可執行它們的主機；GitHub Pages 也無法執行這兩項服務。

## 1. 決定上傳範圍

第一次可以只上傳 README.md 和 docs/ 的文件。之後再補齊程式碼、設定範例與測試腳本。

目前工作目錄已存在 `.git`，使用 SourceTree 的 Add 加入現有儲存庫即可，不需要再次 Create。

如果上傳的是另一個沒有 `.git` 的實際專案資料夾，才使用 Create 建立本機 Git 儲存庫。

## 2. 上傳程式碼前整理

目前發現後端存在 `src/main/resources/keys/private.pem`。不要提交這個 JWT 私鑰；另行提供本機產生或設定金鑰的說明。檢查 application.properties 是否包含真實密碼、帳號或私人路徑，改用設定範例或環境變數後再上傳。

前端目前缺少 package.json，應從實際執行中的完整前端專案補齊。node_modules、target、測試結果與暫存檔不需要上傳。

可以在根目錄建立 `.gitignore`，加入以下規則，並依實際專案補充：

```gitignore
node_modules/
target/
dist/
tmp/
.env
.env.*
!.env.example
**/private.pem
*.jtl
jmeter.log
ApacheJMeterTemporaryRootCA.*
proxyserver.jks
artifacts/
```

`.gitignore` 不會移除已經提交過的資料。若私鑰或密碼已公開，需更換憑證，並處理 Git 歷史；單純刪除最新版本不夠。

## 3. 在 GitHub 建立空白 Repository

1. 登入 GitHub，選 New repository。
2. Repository name 可填 `bakery-shopping-cart`。
3. 選 Public 或 Private；Public 的內容任何人都能讀取。
4. 此流程由本機推送，先不要勾選初始化 README、.gitignore 或 License，避免產生另一段歷史。
5. 按 Create repository。
6. 複製 HTTPS 網址，格式為 `https://github.com/你的帳號/bakery-shopping-cart.git`。

## 4. SourceTree 加入本機專案

1. 開啟 SourceTree，在新分頁選 Add（加入既有本機儲存庫）。
2. 選取包含 `.git`、bakeryweb、bakeryusercart、README.md 的外層資料夾。
3. 加入後開啟儲存庫，進入 File Status。

實際按鈕名稱可能因 SourceTree 語言與版本略有不同。

## 5. 建立 Commit

1. 在 Unstaged files 找到 README.md 與 docs/ 下的兩份教學。
2. 只勾選這三份文件，加入 Staged files。
3. 查看差異，確認內容是預期要公開的文字。
4. 輸入訊息：`docs: add project overview and JMeter guide`。
5. 按 Commit。

第一次若要求 Git 姓名與 Email，設定你希望出現在提交紀錄的身分；也可以使用 GitHub 提供的 noreply Email。

Commit 只寫入本機歷史，尚未上傳 GitHub。

## 6. 設定遠端

1. 開啟 Repository Settings / Settings。
2. 找到 Remotes，按 Add。
3. Remote name 填 `origin`。
4. URL 貼上剛剛建立的 GitHub HTTPS 網址。
5. 儲存。

若已有 origin，先確認是否就是目標 GitHub 儲存庫，不要直接覆蓋不明的遠端設定。

## 7. Push 到 GitHub

1. 按 Push，遠端選 origin。
2. 勾選本機目前分支，將它推送到遠端同名分支。
3. 若本機叫 master，可以先使用該名稱；不必假設一定叫 main。希望統一 main 時再重新命名。
4. 若有 Track 選項，勾選以建立追蹤關係。
5. 按 Push，依登入提示完成 GitHub 授權。

GitHub 的 HTTPS Git 操作不使用一般帳號密碼。優先依瀏覽器或憑證管理員登入；若環境要求 token，使用有目標儲存庫寫入權限的 Personal Access Token，不要將 token 貼進文件或網址。

Push 成功後重新整理 GitHub 頁面，確認三份 Markdown 都存在且 README 顯示正常。

若出現 non-fast-forward / rejected，代表遠端已有本機沒有的提交；先確認遠端內容再整合，不要直接 Force Push。

## 8. 之後更新

```text
修改檔案 → 查看差異 → Stage → Commit → Push
```

多人協作時，開始前先 Fetch / Pull 並處理衝突。測試腳本整理完成後，再加入 tests/jmeter/，最後才設計 CI pipeline。

## 官方參考

- GitHub：https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github
- SourceTree：https://support.atlassian.com/sourcetree/
