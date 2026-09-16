import { useState } from 'react'
import { login } from '../api/ApiService'


function Login({ onLoginSuccess, setCurrentPage }) {

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState('')


  async function handleLogin(e) {
    e.preventDefault()

    setErrorMsg('')

    try {
    // ApiService 會將 Access Token 保存到記憶體
    // 瀏覽器會自動接收後端設定的 Refresh Token Cookie
      const result = await login(username, password)

    // 清除舊版登入功能留下的 token
      localStorage.removeItem('token')

    // 使用後端回傳的會員帳號更新畫面
      onLoginSuccess(result.username)

      alert('登入成功！')
    } catch (error) {
      setErrorMsg(error.message || '登入失敗，請稍後再試')
    }
  }

  return (

    <main className="login-page">

      <section className="login-heading">

        <p className="login-subtitle">
          MEMBER LOGIN
        </p>

        <h1>帳戶登入</h1>

        <div className="title-line"></div>

      </section>


      <section className="login-container">

        <div className="login-box">

          <div className="login-intro">

            <h2>會員登入</h2>

            <p>
              登入帳戶即可查看訂單與購物紀錄。
            </p>

          </div>


          <form onSubmit={handleLogin}>

            <div className="login-field">

              <label htmlFor="username">
                帳號
              </label>

              <input
                id="username"
                type="text"
                placeholder="請輸入帳號"
                value={username}
                onChange={e =>
                  setUsername(e.target.value)
                }
                required
              />

            </div>


            <div className="login-field">

              <label htmlFor="password">
                密碼
              </label>

              <input
                id="password"
                type="password"
                placeholder="請輸入密碼"
                value={password}
                onChange={e =>
                  setPassword(e.target.value)
                }
                required
              />

            </div>


            {errorMsg && (

              <div className="login-error">
                {errorMsg}
              </div>

            )}


            <div className="login-button-group">

              <button
                type="submit"
                className="login-button"
              >
                登入
              </button>


              <button
                type="button"
                className="register-button"
                onClick={() =>
                  setCurrentPage('register')
                }
              >
                加入會員
              </button>

            </div>

          </form>

        </div>

      </section>

    </main>
  )
}

export default Login