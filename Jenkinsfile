pipeline {
  agent {
    docker {
      image 'maven:3.3.3'
    }
  }
  stages {
    stage('build') {
        steps {
            sh 'mvn --version'
          }
        }
        stage('trnds') {
          steps {
            echo 'Starting stage: trnds...'
            load "src/main/groovy/TrendFetcher.groovy"
            echo 'Completed stage: trnds'
          }
        }
   }
}