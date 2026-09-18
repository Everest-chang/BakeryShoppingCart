pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
    }

    stages {
        stage('下載程式碼') {
            steps {
                deleteDir()

                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Everest-chang/BakeryShoppingCart.git'
                    ]]
                ])
            }
        }

        stage('前端安裝依賴') {
            steps {
                dir('bakeryweb') {
                    bat 'call npm.cmd ci'
                }
            }
        }

        stage('前端程式碼檢查') {
            steps {
                dir('bakeryweb') {
                    bat 'call npm.cmd run lint'
                }
            }
        }

        stage('前端建置') {
            steps {
                dir('bakeryweb') {
                    bat 'call npm.cmd run build'
                }
            }
        }

        stage('後端測試與打包') {
            steps {
                dir('bakeryusercart') {
                    bat 'call mvnw.cmd -B clean verify'
                }
            }

            post {
                always {
                    junit(
                        testResults:
                            'bakeryusercart/target/surefire-reports/TEST-*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }
    }
}