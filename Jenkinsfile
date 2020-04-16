#!groovy
stage('Test') {
    node {

        checkout scm

        def build = "${env.JOB_NAME} - #${env.BUILD_NUMBER}".toString()

        currentBuild.result = "SUCCESS"

        try {
            sh './gradlew clean run'
            try {
                sh './gradlew build'
            } finally {
                junit "build/test-results/**/*.xml"
            }
        } catch (err) {
            currentBuild.result = "FAILURE"

            emailext to: "${env.EMAIL}",
                    recipientProviders: [[$class: 'DevelopersRecipientProvider']],
                    body: "${env.JOB_NAME} failed! See ${env.BUILD_URL} for details.",
                    subject: "$build failed!"

            throw err
        }
    }
}
