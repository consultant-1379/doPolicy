def bobVersion = '0.0.4'
def bob = "docker -D run --rm " +
        '--env APP_PATH="`pwd`" ' +
        '--env RELEASE=${RELEASE} ' +
        "-v \"`pwd`:`pwd`\" " +
        "-v /var/run/docker.sock:/var/run/docker.sock " +
        "armdocker.rnd.ericsson.se/proj-orchestration-so/bob:${bobVersion}"

pipeline {
    agent {
        node {
            label params.SLAVE
        }
    }
    stages {
            stage('Clean') {
            steps {
                sh "${bob} clean"
                sh 'git clean -xdff --exclude=.m2 --exclude=.sonar'
            }
        }
        stage('Lint') {
            steps {
                sh "${bob} lint"
            }
        }
        stage('Build package and execute tests') {
            steps {
                sh "${bob} build"
            }
        }

        stage('Build image and chart') {
            steps {
			 sh "${bob} image"
            }
        }
        stage('Run SonarQube analysis') {
            steps {
                sh "${bob} sonar -v"
            }
        } 
        stage('Push image and chart') {
            when {
                expression { params.RELEASE == "true" }
            }
            steps {
                sh "${bob} push"
                
            }
        }
        stage('Generate input for SD staging'){
            when {
                expression { params.RELEASE == "true" }
            }
            steps{
                sh "${bob} generate-output-parameters"
                archiveArtifacts 'artifact.properties'
            }
        }
    }
    post {
        always {
            archive "**/target/surefire-reports/*"
            junit '**/target/surefire-reports/*.xml'
            step( [ $class: 'JacocoPublisher' ] )
        }
    }
}
