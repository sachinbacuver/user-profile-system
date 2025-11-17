pipeline {
    agent any



    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Checked out ${env.GIT_COMMIT ?: 'unknown commit'}"
            }
        }

        stage('Build & Test (parallel)') {
            steps {
                script {
                    def branches = [:]

                    branches['Build & Test user-api'] = {
                        dir('user-api-service') {
                            sh 'mvn -B clean install'
                        }
                    }

                    branches['Build & Test profile-api'] = {
                        dir('profile-api-service') {
                            sh 'mvn -B clean install'
                        }
                    }

                    parallel branches, failFast: true
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
            echo "One or more stages failed — pipeline marked as FAILURE."
        }

        success {
            echo "Both services built and tested successfully."
        }
    }
}
