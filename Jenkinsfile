// Jenkinsfile - CI/CD Pipeline for Booking System (Render Deployment)

pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'booking-system'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'shisir27'
        
        // Render API key stored in Jenkins credentials
        RENDER_API_KEY = credentials('render-api-key')
        RENDER_SERVICE_ID = 'srv-d4slknnpm1nc73c639k0'
    }
    
    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                // checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Java application...'
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
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                echo 'Running code quality checks (placeholder)...'
                // Add SonarQube here if needed
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image ${DOCKER_IMAGE}:${DOCKER_TAG}..."
                script {
                    docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Docker Push (Optional)') {
            when {
                expression { false }  // Change to true when ready to push to Docker Hub
            }
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        docker.image("${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                    }
                }
            }
        }

        stage('Deploy to Render') {
            steps {
                echo "Deploying to Render service: ${RENDER_SERVICE_ID}"
                script {
                    bat """
                    curl -X POST ^
                    -H "Accept: application/json" ^
                    -H "Authorization: Bearer ${RENDER_API_KEY}" ^
                    https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Performing health check on local build...'
                script {
                    def status = bat(
                        script: "curl --silent --fail http://localhost:3000/actuator/health",
                        returnStatus: true
                    )

                    if (status != 0) {
                        error("❌ Health check FAILED — application not responding.")
                    } else {
                        echo "✔ Health check PASSED — application is healthy."
                    }
                }
            }
        }
    }

    post {
        success {
            echo '🎉 Deployment completed successfully!'
        }
        failure {
            echo '❌ Deployment failed — review logs and consider rollback.'
        }
        always {
            echo 'Pipeline execution complete.'
        }
    }
}
