pipeline {
    agent any

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        AWS_REGION = credentials('AWS_REGION')   // stored in Jenkins
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Checked out ${env.GIT_COMMIT ?: 'unknown commits'}"
            }
        }

        stage('Build & Test (parallel)') {
            steps {
                script {
                    parallel(
                        failFast: true,
                        "Build & Test user-api-service": {
                            sh """
                                cd user-api-service
                                mvn -f pom.xml clean install
                            """
                        },
                        "Build & Test profile-api-service": {
                            sh """
                                cd profile-api-service
                                mvn -f pom.xml clean install
                            """
                        }
                    )
                }
            }
        }

        /*******************************
         *   NEW: Build Docker Images  *
         *******************************/
        stage('Build Images (parallel)') {
            steps {
                script {
                    parallel(
                        "Build user-api image": {
                            sh """
                                cd user-api-service
                                docker build -t user-api-repo .
                            """
                        },
                        "Build profile-api image": {
                            sh """
                                cd profile-api-service
                                docker build -t profile-api-repo .
                            """
                        }
                    )
                }
            }
        }

        /*****************************
         *   NEW: Push to AWS ECR    *
         *****************************/
        stage('Push to ECR') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

                        def userApiRepo = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/user-api-repo"
                        def profileApiRepo = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/profile-api-repo"

                        // Login to ECR
                        sh """
                            aws ecr get-login-password --region ${env.AWS_REGION} \
                            | docker login --username AWS --password-stdin 111851026561.dkr.ecr.${env.AWS_REGION}.amazonaws.com
                        """

                        // Tag the images
                        sh """
                            docker tag user-api-repo ${userApiRepo}:latest
                            docker tag profile-api-repo ${profileApiRepo}:latest
                        """

                        // Push to ECR
                        sh """
                            docker push ${userApiRepo}:latest
                            docker push ${profileApiRepo}:latest
                        """
                    }
                }
            }
        }

        /*******************************
         *       NEW: Deploy           *
         *******************************/
        stage('Deploy') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

                        echo "Updating ECS Service..."
						echo "Updating user-api-service..."
                        sh """
                            aws ecs update-service \
                                --cluster user-profile-cluster \
                                --service user-api-service \
                                --force-new-deployment
                        """
						
						echo "Updating user-api-service..."
						sh """
                            aws ecs update-service \
                                --cluster user-profile-cluster \
                                --service profile-api-task-service-sd8td7rr \
                                --force-new-deployment
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'user-api-service/**/target/surefire-reports/*.xml,profile-api-service/**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'user-api-service/**/target/*.jar,profile-api-service/**/target/*.jar', allowEmptyArchive: true
            cleanWs()
        }

        failure {
            echo "Pipeline FAILED ❌"
        }

        success {
            echo "Pipeline COMPLETED Successfully ✔"
        }
    }
}
