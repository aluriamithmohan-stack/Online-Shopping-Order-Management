pipeline {
    agent any

    tools {
        nodejs 'NodeJS_20'
        maven  'Maven_3.9'
    }

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
                bat '''
                    start /B node server.js
                    timeout /t 5 /nobreak
                    node tests/functional.test.js
                '''
            }
        }

        stage('4. Run Selenium Automated Tests') {
            steps {
                bat 'mvn -f selenium-tests/pom.xml test -Dtest=MandatoryTests -DbaseUrl=http://localhost:3000 -Dheadless=true'
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
            junit 'selenium-tests/target/surefire-reports/*.xml'
        }
        success {
            echo "CI/CD Pipeline Successful! All 7 mandatory test cases passed."
        }
        failure {
            echo "Pipeline Failed. Review execution logs."
        }
    }
}
