pipeline {
    agent any

    environment {
        PORT = '3000'
        HOST = '0.0.0.0'
        BASE_URL = 'http://localhost:3000'
    }

    stages {
        stage('1. Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('2. Install Dependencies') {
            steps {
                bat 'npm install'
            }
        }

        stage('3. Run Functional Tests') {
            steps {
                bat 'npm test'
            }
        }

        stage('4. Run Selenium Automated Tests') {
            steps {
                bat 'npm run test:selenium'
            }
        }

        stage('5. Docker Build & Tag') {
            steps {
                bat '''
                    docker build -t shopping-system:%BUILD_NUMBER% .
                    docker tag shopping-system:%BUILD_NUMBER% shopping-system:latest
                '''
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'selenium-tests/target/surefire-reports/*.xml'
        }
        success {
            echo "CI/CD Pipeline Successful! All 7 mandatory test cases passed."
        }
        failure {
            echo "Pipeline Failed. Review execution logs."
        }
    }
}
