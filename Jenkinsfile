pipeline {
    agent any
	
	environment {
	DOCKER_HOST = 'unix:///var/run/docker.sock'
    TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
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

    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'user-api-service/**/target/surefire-reports/*.xml,profile-api-service/**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'user-api-service/**/target/*.jar,profile-api-service/**/target/*.jar', allowEmptyArchive: true
            cleanWs()
        }

        failure {
            echo "One or more stages failed — pipeline marked as a FAILURE."
        }

        success {
            echo "Both services built and tested successfully."
        }
    }
}
