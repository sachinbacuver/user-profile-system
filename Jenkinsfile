pipeline {
    agent any

    // Useful pipeline-wide options
    options {
        // keep only last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // timestamps in the console log
        timestamps()
        // fail the pipeline if any step takes longer than N minutes (optional)
        timeout(time: 60, unit: 'MINUTES')
    }

    // Don't let Jenkins automatically checkout twice if you want fine control
    // (we will explicitly checkout in the Checkout stage).
    triggers {  } 
    stages {
        stage('Checkout') {
            steps {
                // single checkout for the whole repo
                checkout scm
                echo "Checked out ${env.GIT_COMMIT ?: 'unknown commit'}"
            }
        }

        // A declarative stage that runs a scripted parallel block so we can use failFast
        stage('Build & Test (parallel)') {
            steps {
                script {
                    // Prepare a map of parallel branches
                    def branches = [:]

                    branches['Build & Test user-api'] = {
                        // Each branch runs in the same workspace, so isolate builds by using directories.
                        dir('user-api-service') {
                            // run mvn in batch mode (-B). If tests fail maven exits non-zero -> Jenkins fails this branch.
                            sh 'mvn -B clean install'
                        }
                    }

                    branches['Build & Test profile-api'] = {
                        dir('profile-api-service') {
                            sh 'mvn -B clean install'
                        }
                    }

                    // Run the branches in parallel. failFast:true will cancel other branches
                    // as soon as one branch fails — useful to save time.
                    parallel branches, failFast: true
                }
            }
        }
    }

    post {
        // Always publish test results if present (useful for visibility even on failure)
        always {
            // collect JUnit XML results from both modules (adjust patterns to your build output)
            junit allowEmptyResults: true, testResults: 'user-api-service/**/target/surefire-reports/*.xml,profile-api-service/**/target/surefire-reports/*.xml'
            // save built artifacts (jars) for debugging
            archiveArtifacts artifacts: 'user-api-service/**/target/*.jar,profile-api-service/**/target/*.jar', allowEmptyArchive: true
            cleanWs() // optional: clean workspace after the run
        }

        failure {
            echo "One or more stages failed — pipeline marked as FAILURE."
        }

        success {
            echo "Both services built and tested successfully."
        }
    }
}
