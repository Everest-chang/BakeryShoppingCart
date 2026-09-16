import { useState } from 'react'
import { register } from '../api/ApiService'


function Register({ setCurrentPage }) {

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [errorMsg, setErrorMsg] = useState('')
  
 async function handleRegister(e) {

  e.preventDefault()

  try {

    await register(
      username,
      password,
      name,
      email
    )

    setErrorMsg('')

    alert('會員建立成功！')

    setCurrentPage('login')

  } catch (error) {

    setErrorMsg('會員建立失敗！')

  }
}
//   async function handleRegister(e) {
//     e.preventDefault()

//     try {
//         console.log("1. 準備呼叫 register")

//         const message = await register(
//             username,
//             password,
//             name,
//             email
//         )

//         console.log("2. register 成功回來")
//         console.log("message =", message)

//         setErrorMsg('')

//         console.log("3. setErrorMsg 完成")

//         alert('會員建立成功！')

//     } catch (error) {
//         console.log("進入 catch")
//         console.error("真正錯誤：", error)

//         setErrorMsg(error.message)
//     }
// }


  return (

    <main className="register-page">


      <section className="register-heading">

        <p className="register-subtitle">
          CREATE ACCOUNT
        </p>

        <h1>加入會員</h1>

        <div className="title-line"></div>

      </section>


      <section className="register-container">

        <div className="register-box">


          <div className="register-intro">

            <h2>建立新帳戶</h2>

            <p>
              填寫以下資料即可建立會員帳戶。
            </p>

          </div>


          <form onSubmit={handleRegister}>


            {/* 帳號 */}

            <div className="register-field">

              <label htmlFor="registerUsername">
                帳號
              </label>

              <input
                id="registerUsername"
                type="text"
                placeholder="請輸入帳號"
                value={username}
                onChange={e =>
                  setUsername(e.target.value)
                }
                required
              />

            </div>


            {/* 密碼 */}

            <div className="register-field">

              <label htmlFor="registerPassword">
                密碼
              </label>

              <input
                id="registerPassword"
                type="password"
                placeholder="請輸入密碼"
                value={password}
                onChange={e =>
                  setPassword(e.target.value)
                }
                required
              />

            </div>


            {/* 姓名 */}

            <div className="register-field">

              <label htmlFor="name">
                姓名
              </label>

              <input
                id="name"
                type="text"
                placeholder="請輸入姓名"
                value={name}
                onChange={e =>
                  setName(e.target.value)
                }
                required
              />

            </div>


            {/* 信箱 */}

            <div className="register-field">

              <label htmlFor="email">
                信箱
              </label>

              <input
                id="email"
                type="email"
                placeholder="請輸入電子信箱"
                value={email}
                onChange={e =>
                  setEmail(e.target.value)
                }
                required
              />

            </div>


            {errorMsg && (

              <div className="register-error">
                {errorMsg}
              </div>

            )}


            <div className="register-button-group">

              <button
                type="submit"
                className="register-submit-button"
              >
                建立會員
              </button>


              <button
                type="button"
                className="register-back-button"
                onClick={() =>
                  setCurrentPage('login')
                }
              >
                返回登入
              </button>

            </div>

          </form>

        </div>

      </section>

    </main>
  )
}


export default Register