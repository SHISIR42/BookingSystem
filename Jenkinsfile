// Jenkinsfile sync test comment
pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'booking-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'shisir27'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from repository...'
                // checkout scm
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
                // sh 'mvn sonar:sonar'
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo 'Building Docker image...'
                script {
                    def img = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.build("${DOCKER_IMAGE}:latest")

                    // Push only if registry configured
                    // docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                    //     img.push()
                    //     docker.image("${DOCKER_IMAGE}:latest").push()
                    // }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Triggering deployment...'
                // Add deployment script or API call for Render
            }
        }

      stage('Health Check') {
    steps {
        echo 'Checking application health...'
        script {
            def status = bat(
                script: "curl --silent --fail http://localhost:3000/actuator/health",
                returnStatus: true
            )

            if (status != 0) {
                error("❌ Health check failed! Application is NOT running.")
            } else {
                echo "✔ Application is healthy."
            }
        }
    }
}
    }

    post {
        failure {
            echo '❌ Deployment failed. Rollback to previous version if possible.'
        }
        always {
            echo 'Pipeline finished. Check Render dashboard or local logs for monitoring.'
        }
    }
}
