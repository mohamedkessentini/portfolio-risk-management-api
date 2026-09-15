// Jenkinsfile for portfolio-risk-management-api
//
// Prerequisites on the Jenkins controller/agent (see docs/JENKINS.md):
//   - JDK 21 and Maven configured as Jenkins global tools named "jdk21" and "maven3"
//   - Docker CLI available on the agent (Docker-outside-of-Docker: mount the host's
//     /var/run/docker.sock into the Jenkins container) if you want the docker-build stage
//
// Run locally: `docker run -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home
//   -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts` then create a
// Pipeline job pointing at this repository.

pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        IMAGE_NAME = 'portfolio-risk-management-api'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests compile'
            }
        }

        stage('Unit Tests') {
            steps {
                sh "mvn -B -Dtest='!*IntegrationTest' test"
            }
            post {
                always {
                    junit 'target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Integration Tests') {
            when {
                expression { return sh(script: 'docker info > /dev/null 2>&1', returnStatus: true) == 0 }
            }
            steps {
                sh "mvn -B -Dtest='*IntegrationTest' test"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            when {
                expression { return sh(script: 'docker info > /dev/null 2>&1', returnStatus: true) == 0 }
            }
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
            }
        }
    }

    post {
        success {
            echo "Build #${BUILD_NUMBER} succeeded."
        }
        failure {
            echo "Build #${BUILD_NUMBER} failed - check the stage logs above."
        }
    }
}
