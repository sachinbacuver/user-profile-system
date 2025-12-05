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
		
		stage('Detect Changes') {
			steps {
				script {
					env.CHANGED_USER_API      = sh(script: "git diff --name-only HEAD~1 HEAD | grep '^user-api-service/' || true", returnStdout: true).trim()
					env.CHANGED_PROFILE_API   = sh(script: "git diff --name-only HEAD~1 HEAD | grep '^profile-api-service/' || true", returnStdout: true).trim()
					env.CHANGED_NOTIFICATION  = sh(script: "git diff --name-only HEAD~1 HEAD | grep '^notification-service/' || true", returnStdout: true).trim()

					echo "Changed User API: ${env.CHANGED_USER_API}"
					echo "Changed Profile API: ${env.CHANGED_PROFILE_API}"
					echo "Changed Notification: ${env.CHANGED_NOTIFICATION}"
				}
			}
		}


        /*******************************
         * BUILD AND TEST
         *******************************/
        stage('Build & Test (parallel)') {
			steps {
				script {
					def tasks = [:]

					if (env.CHANGED_USER_API) {
						tasks["Build & Test user-api-service"] = {
							sh "cd user-api-service && mvn -f pom.xml clean install"
						}
					}

					if (env.CHANGED_PROFILE_API) {
						tasks["Build & Test profile-api-service"] = {
							sh "cd profile-api-service && mvn -f pom.xml clean install"
						}
					}

					if (env.CHANGED_NOTIFICATION) {
						tasks["Build & Test notification-service"] = {
							sh "cd notification-service && mvn -f pom.xml clean install"
						}
					}

					parallel tasks
				}
			}
		}


        /*******************************
         * BUILD DOCKER IMAGES
         *******************************/
        stage('Build Images (conditional)') {
			steps {
				script {
					def dockerTasks = [:]

					if (env.CHANGED_USER_API) {
						dockerTasks["Build user-api image"] = {
							sh "cd user-api-service && docker build -t user-api-repo ."
						}
					}
					if (env.CHANGED_PROFILE_API) {
						dockerTasks["Build profile-api image"] = {
							sh "cd profile-api-service && docker build -t profile-api-repo ."
						}
					}

					if (dockerTasks) {
						parallel dockerTasks
					} else {
						echo "No Docker services changed. Skipping image builds."
					}
				}
			}
		}


        /*******************************
         * PUSH TO ECR
         *******************************/
   
		stage('Push to ECR') {
			when {
				expression { env.CHANGED_USER_API || env.CHANGED_PROFILE_API }
			}
			steps {
				script {
					withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {
						sh """
							aws ecr get-login-password --region ${env.AWS_REGION} \
							| docker login --username AWS --password-stdin 111851026561.dkr.ecr.${env.AWS_REGION}.amazonaws.com
						"""

						if (env.CHANGED_USER_API) {
							sh """
								docker tag user-api-repo ${userApiRepo}:latest
								docker push ${userApiRepo}:latest
							"""
						}

						if (env.CHANGED_PROFILE_API) {
							sh """
								docker tag profile-api-repo ${profileApiRepo}:latest
								docker push ${profileApiRepo}:latest
							"""
						}
					}
				}
			}
		}


        
		
		
		stage('Deploy User API') {
			when { expression { env.CHANGED_USER_API } }
			steps {
				script {
					withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

						def userApiImage    = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/user-api-repo:latest"

						/**********************************************
						 * 1️⃣ USER API – FETCH CURRENT TASK DEFINITION
						 **********************************************/
						echo "Fetching existing task definition for user-api-task..."

						sh """
							USER_TASKDEF_ARN=\$(aws ecs describe-services \
								--cluster user-profile-cluster \
								--services user-api-service \
								--query 'services[0].taskDefinition' \
								--output text)

							echo "User API TaskDef ARN: \$USER_TASKDEF_ARN"

							aws ecs describe-task-definition \
								--task-definition \$USER_TASKDEF_ARN \
								--query 'taskDefinition' \
								> user-api-service/current_taskdef.json
						"""

						/**********************************************
						 * 2️⃣ CLEAN JSON (remove status/family/revision)
						 **********************************************/
						sh """
							jq 'del(.compatibilities, .requiresAttributes, .taskDefinitionArn, .revision, .status, .registeredAt, .registeredBy)' \
								user-api-service/current_taskdef.json \
								> user-api-service/clean_taskdef.json
						"""

						/**********************************************
						 * 3️⃣ UPDATE IMAGE
						 **********************************************/
						sh """
							jq --arg IMAGE "${userApiImage}" \
								'.containerDefinitions[0].image=\$IMAGE' \
								user-api-service/clean_taskdef.json \
								> user-api-service/new_taskdef.json

							echo "===== NEW USER TASKDEF ====="
							cat user-api-service/new_taskdef.json
						"""

						/**********************************************
						 * 4️⃣ REGISTER NEW REVISION
						 **********************************************/
						sh """
							aws ecs register-task-definition \
								--cli-input-json file://user-api-service/new_taskdef.json
						"""

						/**********************************************
						 * 5️⃣ DEPLOY NEW REVISION
						 **********************************************/
						sh """
							aws ecs update-service \
								--cluster user-profile-cluster \
								--service user-api-service \
								--task-definition user-api-task \
								--force-new-deployment
						"""
					}
				}
			}
		}
		
		stage('Deploy Profile API') {
			when { expression { env.CHANGED_PROFILE_API } }
			steps {
				script {
					withAWS(credentials: 'aws-creds', region: env.AWS_REGION) {

						def profileApiImage = "111851026561.dkr.ecr.ap-south-1.amazonaws.com/profile-api-repo:latest"


						echo "Fetching existing task definition for profile-api-task..."

						sh """
							PROFILE_TASKDEF_ARN=\$(aws ecs describe-services \
								--cluster user-profile-cluster \
								--services profile-api-task-service-sd8td7rr \
								--query 'services[0].taskDefinition' \
								--output text)

							echo "Profile API TaskDef ARN: \$PROFILE_TASKDEF_ARN"

							aws ecs describe-task-definition \
								--task-definition \$PROFILE_TASKDEF_ARN \
								--query 'taskDefinition' \
								> profile-api-service/current_taskdef.json
						"""

						sh """
							jq 'del(.compatibilities, .requiresAttributes, .taskDefinitionArn, .revision, .status, .registeredAt, .registeredBy)' \
								profile-api-service/current_taskdef.json \
								> profile-api-service/clean_taskdef.json
						"""

						sh """
							jq --arg IMAGE "${profileApiImage}" \
								'.containerDefinitions[0].image=\$IMAGE' \
								profile-api-service/clean_taskdef.json \
								> profile-api-service/new_taskdef.json
						"""

						sh """
							aws ecs register-task-definition \
								--cli-input-json file://profile-api-service/new_taskdef.json
						"""

						sh """
							aws ecs update-service \
								--cluster user-profile-cluster \
								--service profile-api-task-service-sd8td7rr \
								--task-definition profile-api-task \
								--force-new-deployment
						"""

					}
				}
			}
		}

		
		stage('Upload Notification JAR to S3') {
			when { expression { env.CHANGED_NOTIFICATION } }
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
			when { expression { env.CHANGED_NOTIFICATION } }
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
