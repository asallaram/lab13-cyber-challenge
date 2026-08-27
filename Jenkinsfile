pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('starter') {
                    sh 'mvn -B clean compile'
                }
            }
        }

        stage('Test') {
            steps {
                dir('starter') {
                    sh 'mvn -B test'
                }
            }
        }

        stage('Package') {
            steps {
                dir('starter') {
                    sh 'mvn -B package -DskipTests'
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'starter/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'starter/target/*.jar', allowEmptyArchive: true
        }
    }
}
