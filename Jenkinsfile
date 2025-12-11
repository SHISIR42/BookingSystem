pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'booking-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'your-dockerhub-username'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from repository...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building the application...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean package -DskipTests'
                    } else {
                        bat 'mvn clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    echo 'Test results published'
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                echo 'Analyzing code quality...'
                // Add SonarQube analysis if configured
                // sh 'mvn sonar:sonar'
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo 'Building and pushing Docker image...'
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.build("${DOCKER_IMAGE}:latest")
                    // Uncomment and configure credentials for Docker Hub push
                    // docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                    //     docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                    //     docker.image("${DOCKER_IMAGE}:latest").push()
                    // }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Triggering deployment (manual or via Render)...'
                // Add deployment script or Render API call here if available
            }
        }

        stage('Health Check') {
            steps {
                echo 'Checking application health...'
                script {
                    // Example: curl health endpoint and check status
                    def result = bat(script: 'curl -s -o NUL -w "%{http_code}" http://localhost:8080/actuator/health', returnStdout: true).trim()
                    if (result != '200') {
                        error("Health check failed! Rolling back...")
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Deployment failed. Rollback to previous version if possible.'
            // Add rollback logic here (e.g., redeploy previous Docker image)
        }
        always {
            echo 'Pipeline finished. Check Render dashboard or local logs for monitoring.'
        }
    }
}
        
        stage('Deploy to Staging') {
            steps {
                echo 'Deploying to staging environment...'
                script {
                    if (isUnix()) {
                        sh 'docker-compose down'
                        sh 'docker-compose up -d'
                    } else {
                        bat 'docker-compose down'
                        bat 'docker-compose up -d'
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Performing health check...'
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    if (isUnix()) {
                        sh 'curl -f http://localhost:8080/actuator/health || exit 1'
                    } else {
                        bat 'powershell -Command "Invoke-WebRequest -Uri http://localhost:8080 -UseBasicParsing"'
                    }
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                echo 'Deploying to production environment...'
                script {
                    // Add production deployment steps
                    echo 'Production deployment would happen here'
                }
            }
        }
        */
    }
    
    post {
        always {
            echo 'Pipeline execution completed'
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
            // Add notification (email, Slack, etc.)
        }
        failure {
            echo 'Pipeline failed!'
            // Rollback would happen here if deployment stages were active
        }
        unstable {
            echo 'Pipeline is unstable'
        }
    }
}
