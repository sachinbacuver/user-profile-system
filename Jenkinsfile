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
         * CREATE NEW TASK REVISION + DEPLOY
         *******************************/
        stage('Deploy') {
            steps {
                script {
                    withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

                        def userApiImage    = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/user-api-repo:latest"
                        def profileApiImage = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/profile-api-repo:latest"

                        /****************************************
                         * 1️⃣ USER API – New Task Definition Revision
                         ****************************************/
						 sh"""

                        echo "Registering NEW TASK DEFINITION REVISION for user-api..."

                            CURRENT_TASK_DEF=\$(aws ecs describe-services \
                                --cluster user-profile-cluster \
                                --services user-api-service \
                                --query 'services[0].taskDefinition' \
                                --output text)
                            
                            echo "Current task definition: \$CURRENT_TASK_DEF"
                            
                            aws ecs describe-task-definition \
                                --task-definition \$CURRENT_TASK_DEF \
                                --query 'taskDefinition' > user-api-service/current-taskdef.json
						"""
                    }
                }
            }
        }
		 
	}
	
	/*******************************
     * POST ACTIONS
     *******************************/
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
