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

        stage('Checkout') {
            steps {
                checkout scm
                echo "Checked out ${env.GIT_COMMIT ?: 'unknown commit'}"
            }
        }

        stage('Build & Test notification-service') {
            steps {
                script {
                    sh """
                        cd notification-service
                        mvn -f pom.xml clean install
                    """
                }
            }
        }

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

        stage('Update Lambda') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {
                        echo "Updating Lambda Function ${LAMBDA_NAME}..."
                        sh """
                            aws lambda update-function-code \
                                --function-name ${LAMBDA_NAME} \
                                --s3-bucket ${S3_BUCKET} \
                                --s3-key notification-service-latest.jar
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, 
                  testResults: 'notification-service/**/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: 'notification-service/**/target/*.jar', allowEmptyArchive: true
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
