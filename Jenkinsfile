pipeline {
    agent any

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        AWS_REGION = credentials('AWS_REGION')
        S3_BUCKET = "notification-service-lambda-bucket"
        LAMBDA_NAME = "notification-lambda" 
    }

    stages {

        /*******************************
         * CHECKOUT
         *******************************/
        stage('Checkout') {
            steps {
                checkout scm
                echo "Checked out ${env.GIT_COMMIT ?: 'unknown commit'}"
            }
        }

        /*******************************
         * BUILD & TEST ALL SERVICES
         *******************************/
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
                        },

                        "Build & Test notification-service": {
                            sh """
                                cd notification-service
                                mvn -f pom.xml clean install
                            """
                        }
                    )
                }
            }
        }

        /*******************************
         * BUILD DOCKER IMAGES
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

        /*******************************
         * PUSH TO ECR
         *******************************/
        stage('Push to ECR') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

                        def userApiRepo    = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/user-api-repo"
                        def profileApiRepo = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/profile-api-repo"

                        // Login to ECR
                        sh """
                            aws ecr get-login-password --region ${env.AWS_REGION} \
                            | docker login --username AWS --password-stdin 111851026561.dkr.ecr.${env.AWS_REGION}.amazonaws.com
                        """

                        // Tag images
                        sh """
                            docker tag user-api-repo ${userApiRepo}:latest
                            docker tag profile-api-repo ${profileApiRepo}:latest
                        """

                        // Push images
                        sh """
                            docker push ${userApiRepo}:latest
                            docker push ${profileApiRepo}:latest
                        """
                    }
                }
            }
        }

        /*******************************
         * NEW: Upload Notification JAR to S3
         *******************************/
        stage('Upload Notification JAR to S3') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {
                        echo "Uploading notification-service JAR to S3..."
                        sh """
								aws s3 cp notification-service/target/notification-service-1.0.0.jar	s3://${S3_BUCKET}/notification-service-1.0.0.jar
							"""

                    }
                }
            }
        }

        /*******************************
         * NEW: Update Lambda Code
         *******************************/
        stage('Update Lambda') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {
                        echo "Updating Lambda Function ${LAMBDA_NAME}..."
                        sh """
                            aws lambda update-function-code \
                                --function-name ${LAMBDA_NAME} \
                                --s3-bucket ${S3_BUCKET} \
                                --s3-key notification-service-1.0.0.jar
                        """
                    }
                }
            }
        }

        /*******************************
         * CREATE NEW TASK REVISION + DEPLOY ECS
         *******************************/
        stage('Deploy ECS') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

                        def userApiImage    = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/user-api-repo:latest"
                        def profileApiImage = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/profile-api-repo:latest"

                        echo "Registering NEW TASK REVISION for user-api..."
                        sh """
                            sed "s|IMAGE_URI|${userApiImage}|g" user-api-service/taskdef.json \
                                > user-api-service/taskdef_rendered.json
								
								echo "===== Rendered USER API TASKDEF ====="
								cat user-api-service/taskdef_rendered.json


                            aws ecs register-task-definition \
                                --cli-input-json file://user-api-service/taskdef_rendered.json
                        """

                        echo "Registering NEW TASK REVISION for profile-api..."
                        sh """
                            sed "s|IMAGE_URI|${profileApiImage}|g" profile-api-task-service-sd8td7rr/taskdef.json \
                                > profile-api-task-service-sd8td7rr/taskdef_rendered.json

                            aws ecs register-task-definition \
                                --cli-input-json file://profile-api-task-service-sd8td7rr/taskdef_rendered.json
                        """

                        echo "Deploying user-api-service..."
                        sh """
                            aws ecs update-service \
                                --cluster user-profile-cluster \
                                --service user-api-service \
                                --force-new-deployment
                        """

                        echo "Deploying profile-api-service..."
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
            junit allowEmptyResults: true, 
                  testResults: 'user-api-service/**/target/surefire-reports/*.xml,profile-api-service/**/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: '*/**/target/*.jar', allowEmptyArchive: true
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
